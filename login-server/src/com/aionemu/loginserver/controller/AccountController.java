package com.aionemu.loginserver.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.dao.AccountTimeDAO;
import com.aionemu.loginserver.dao.PremiumDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.AccountTime;
import com.aionemu.loginserver.model.ReconnectingAccount;
import com.aionemu.loginserver.network.aion.AionAuthResponse;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.SessionKey;
import com.aionemu.loginserver.network.aion.serverpackets.SM_ACCOUNT_BANNED_2;
import com.aionemu.loginserver.network.aion.serverpackets.SM_ACCOUNT_KICK;
import com.aionemu.loginserver.network.aion.serverpackets.SM_SERVER_LIST;
import com.aionemu.loginserver.network.aion.serverpackets.SM_UPDATE_SESSION;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_ACCOUNT_AUTH_RESPONSE;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_GS_CHARACTER_RESPONSE;
import com.aionemu.loginserver.utils.AccountUtils;
import com.aionemu.loginserver.utils.ExternalAuth;

/**
 * This class is responsible for controlling all account actions
 * 
 * @author KID, SoulKeeper, Neon
 */
public class AccountController {

	/**
	 * Map with accounts that are active on LoginServer or joined GameServer and are not authenticated yet.
	 */
	private static final Map<Integer, LoginConnection> accountsOnLS = new ConcurrentHashMap<>();

	/**
	 * Map with accounts that are reconnecting to LoginServer ie was joined GameServer.
	 */
	private static final Map<Integer, ReconnectingAccount> reconnectingAccounts = new HashMap<>();

	/**
	 * Map with characters count on each gameserver and accounts
	 */
	private static final Map<Integer, Map<Byte, Integer>> accountsGSCharacterCounts = new HashMap<>();

	/**
	 * Removes account from list of connections
	 * 
	 * @param account
	 *          account
	 */
	public static void removeAccountOnLS(Account account) {
		accountsOnLS.remove(account.getId());
	}

	/**
	 * This method is for answering GameServer question about account authentication on GameServer side.
	 */
	public static void checkAuth(SessionKey key, GsConnection gsConnection) {
		LoginConnection con = accountsOnLS.get(key.accountId);

		if (con != null && con.getSessionKey().checkSessionKey(key)) {
			// account is successful logged in on gs remove it from here
			accountsOnLS.remove(key.accountId);

			GameServerInfo gsi = gsConnection.getGameServerInfo();
			Account acc = con.getAccount();

			// Add account to accounts on GameServer list and update accounts last server
			gsi.addAccountToGameServer(acc);

			acc.setLastServer(gsi.getId());
			AccountDAO.updateLastServer(acc.getId(), acc.getLastServer());

			long toll = PremiumDAO.getPoints(acc.getId());
			// Send response to GameServer
			gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, true, acc.getName(), acc.getCreationDate().getTime(), acc.getAccessLevel(), acc.getMembership(), toll, acc.getAllowedHddSerial()));
		} else {
			gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, false, null, 0, (byte) 0, (byte) 0, 0, null));
		}
	}

	public static synchronized void addReconnectingAccount(ReconnectingAccount acc) {
		reconnectingAccounts.put(acc.getAccount().getId(), acc);
	}

	/**
	 * Check if reconnecting account may auth.
	 * 
	 * @param accountId
	 *          id of account
	 * @param loginOk
	 *          loginOk
	 * @param reconnectKey
	 *          reconnect key
	 * @param client
	 *          aion client
	 */
	public static synchronized void authReconnectingAccount(int accountId, int loginOk, int reconnectKey, LoginConnection client) {
		ReconnectingAccount reconnectingAccount = reconnectingAccounts.remove(accountId);

		if (reconnectingAccount != null && reconnectingAccount.getReconnectionKey() == reconnectKey) {
			Account acc = reconnectingAccount.getAccount();

			client.setAccount(acc);
			accountsOnLS.put(acc.getId(), client);
			client.setState(State.AUTHED_LOGIN);
			client.setSessionKey(new SessionKey(client.getAccount()));
			client.sendPacket(new SM_UPDATE_SESSION(client.getSessionKey()));
		} else {
			client.close();
		}
	}

	/**
	 * Tries to authenticate account.<br>
	 * If success returns {@link AionAuthResponse#AUTHED} and sets account object to connection.<br>
	 * If {@link com.aionemu.loginserver.configs.Config#ACCOUNT_AUTO_CREATION} is enabled - creates new account.<br>
	 * 
	 * @param name
	 *          name of account
	 * @param password
	 *          password of account
	 * @param connection
	 *          connection for account
	 * @return Response with error code
	 */
	public static AionAuthResponse login(String name, String password, LoginConnection connection) {
		// if ip is banned
		if (BannedIpController.isBanned(connection.getIP())) {
			return AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP;
		}

		String accountName = name;

		if (Config.useExternalAuth()) {
			ExternalAuth.Response auth = ExternalAuth.authenticate(name, password);
			if (auth == null) {
				return AionAuthResponse.STR_L2AUTH_S_ACCOUNTCACHESERVER_DOWN;
			}

			AionAuthResponse response = AionAuthResponse.getByIdOrDefault(auth.aionAuthResponseId(), AionAuthResponse.STR_L2AUTH_UNKNOWN4);
			if (response != AionAuthResponse.STR_L2AUTH_S_ALL_OK) {
				return response;
			}
			accountName = auth.accountId();
		}

		Account account = loadAccount(accountName);

		// Try to create new account
		if (account == null && Config.ACCOUNT_AUTO_CREATION && accountName != null && !accountName.isEmpty()) {
			account = createAccount(accountName, password);
		}

		// if account not found and not created
		if (account == null) {
			return AionAuthResponse.STR_L2AUTH_S_ACCOUNT_LOAD_FAIL;
		}

		// if not external authentication, verify password hash from database
		if (!Config.useExternalAuth() && !account.getPasswordHash().equals(AccountUtils.encodePassword(password))) {
			return AionAuthResponse.STR_L2AUTH_S_INCORRECT_PWD;
		}

		// if account is not activated
		if (account.getActivated() != 1) {
			return AionAuthResponse.STR_L2AUTH_S_AGREE_GAME;
		}

		// if account expired
		if (AccountTimeController.isAccountExpired(account)) {
			return AionAuthResponse.STR_L2AUTH_S_TIME_EXHAUSTED;
		}

		// if account is banned
		if (AccountTimeController.isAccountPenaltyActive(account)) {
			connection.close(new SM_ACCOUNT_BANNED_2());
			return null;
		}

		// if account is restricted to some ip or mask
		if (account.getIpForce() != null && !NetworkUtils.checkIPMatching(account.getIpForce(), connection.getIP())) {
			return AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP;
		}

		// Do not allow to login two times with same account
		synchronized (AccountController.class) {
			if (GameServerTable.kickAccountFromGameServer(account.getId(), true))
				return AionAuthResponse.STR_L2AUTH_S_ALREADY_LOGIN;

			// If someone is at loginserver, he should be disconnected
			LoginConnection con = accountsOnLS.remove(account.getId());
			if (con != null) {
				con.close(new SM_ACCOUNT_KICK(AionAuthResponse.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN));
				return AionAuthResponse.STR_L2AUTH_S_ALREADY_LOGIN;
			}
			connection.setAccount(account);
			accountsOnLS.put(account.getId(), connection);
		}

		AccountTimeController.updateOnLogin(account);

		// if everything was OK
		AccountDAO.updateLastIp(account.getId(), connection.getIP());
		// last mac is updated after receiving packet from gameserver
		AccountDAO.updateMembership(account.getId());

		return AionAuthResponse.STR_L2AUTH_S_ALL_OK;
	}

	/**
	 * Kicks account from LoginServer and GameServers
	 * 
	 * @param accountId
	 *          account ID to kick
	 */
	public static void kickAccount(int accountId) {
		synchronized (AccountController.class) {
			GameServerTable.kickAccountFromGameServer(accountId, false);

			LoginConnection conn = accountsOnLS.remove(accountId);
			if (conn != null)
				conn.close(new SM_ACCOUNT_KICK(AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP));
		}
	}

	/**
	 * Loads account from DB and returns it, or returns null if account was not loaded
	 * 
	 * @param name
	 *          acccount name
	 * @return loaded account or null
	 */
	public static Account loadAccount(String name) {
		Account account = AccountDAO.getAccount(name);
		if (account != null)
			setAccountTime(account);
		return account;
	}

	public static Account loadAccount(int id) {
		Account account = AccountDAO.getAccount(id);
		if (account != null)
			setAccountTime(account);
		return account;
	}

	private static void setAccountTime(Account account) {
		AccountTime accTime = AccountTimeDAO.getAccountTime(account.getId());
		if (accTime == null)
			throw new NullPointerException("Account Time for account " + account + " is null");
		account.setAccountTime(accTime);
	}

	/**
	 * Creates new account and stores it in DB. Returns account object in case of success or null if failed
	 * 
	 * @param name
	 *          account name
	 * @param password
	 *          account password
	 * @return account object or null
	 */
	public static Account createAccount(String name, String password) {
		String passwordHash = Config.useExternalAuth() ? "" : AccountUtils.encodePassword(password);
		Account account = new Account();

		account.setName(name);
		account.setPasswordHash(passwordHash);
		account.setAccessLevel((byte) 0);
		account.setMembership((byte) 0);
		account.setActivated((byte) 1);

		if (AccountDAO.insertAccount(account)) {
			return account;
		}
		return null;
	}

	public static synchronized void loadGSCharactersCount(int accountId) {
		Map<Byte, Integer> accountCharacterCount = new HashMap<>();
		for (GameServerInfo gsi : GameServerTable.getGameServers()) {
			GsConnection gsc = gsi.getConnection();

			if (gsc != null)
				gsc.sendPacket(new SM_GS_CHARACTER_RESPONSE(accountId));
			else
				accountCharacterCount.put(gsi.getId(), 0);
		}
		accountsGSCharacterCounts.put(accountId, accountCharacterCount);

		if (hasAllGSCharacterCounts(accountId))
			sendServerListFor(accountId);
	}

	/**
	 * @param accountId
	 * @return
	 */
	public static synchronized boolean hasAllGSCharacterCounts(int accountId) {
		Map<Byte, Integer> characterCount = accountsGSCharacterCounts.get(accountId);
		return characterCount != null && characterCount.size() == GameServerTable.size();
	}

	/**
	 * SM_SERVER_LIST call
	 * 
	 * @param accountId
	 */
	public static void sendServerListFor(int accountId) {
		LoginConnection con = accountsOnLS.get(accountId);
		if (con != null)
			con.sendPacket(new SM_SERVER_LIST());
	}

	/**
	 * @param accountId
	 * @return
	 */
	public static Map<Byte, Integer> getGSCharacterCountsFor(int accountId) {
		return accountsGSCharacterCounts.get(accountId);
	}

	/**
	 * @param accountId
	 * @param gsid
	 * @param characterCount
	 */
	public static synchronized void addGSCharacterCountFor(int accountId, byte gsid, int characterCount) {
		accountsGSCharacterCounts.computeIfAbsent(accountId, k -> new HashMap<>()).put(gsid, characterCount);
	}

	public static void updateServerListForAllLoggedInPlayers() {
		accountsOnLS.values().forEach(con -> {
			if (con.getState() == State.AUTHED_LOGIN && !con.isJoinedGs() && hasAllGSCharacterCounts(con.getAccount().getId()))
				con.sendPacket(new SM_SERVER_LIST());
		});
	}
}
