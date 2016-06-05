package com.aionemu.loginserver.controller;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.dao.AccountTimeDAO;
import com.aionemu.loginserver.dao.PremiumDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.AccountTime;
import com.aionemu.loginserver.model.ExternalAuth;
import com.aionemu.loginserver.model.ReconnectingAccount;
import com.aionemu.loginserver.network.aion.AionAuthResponse;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.SessionKey;
import com.aionemu.loginserver.network.aion.serverpackets.SM_ACCOUNT_KICK;
import com.aionemu.loginserver.network.aion.serverpackets.SM_SERVER_LIST;
import com.aionemu.loginserver.network.aion.serverpackets.SM_UPDATE_SESSION;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_ACCOUNT_AUTH_RESPONSE;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_GS_CHARACTER_RESPONSE;
import com.aionemu.loginserver.utils.AccountUtils;
import com.aionemu.loginserver.utils.ExternalAuthUtil;
import com.mysql.jdbc.StringUtils;

/**
 * This class is responsible for controlling all account actions
 * 
 * @author KID, SoulKeeper
 * @modified Neon
 */
public class AccountController {

	/**
	 * Map with accounts that are active on LoginServer or joined GameServer and are not authenticated yet.
	 */
	private static final Map<Integer, LoginConnection> accountsOnLS = new HashMap<Integer, LoginConnection>();

	/**
	 * Map with accounts that are reconnecting to LoginServer ie was joined GameServer.
	 */
	private static final Map<Integer, ReconnectingAccount> reconnectingAccounts = new HashMap<Integer, ReconnectingAccount>();

	/**
	 * Map with characters count on each gameserver and accounts
	 */
	private static final Map<Integer, Map<Integer, Integer>> accountsGSCharacterCounts = new HashMap<Integer, Map<Integer, Integer>>();

	/**
	 * Removes account from list of connections
	 * 
	 * @param account
	 *          account
	 */
	public static synchronized void removeAccountOnLS(Account account) {
		accountsOnLS.remove(account.getId());
	}

	/**
	 * This method is for answering GameServer question about account authentication on GameServer side.
	 * 
	 * @param key
	 * @param gsConnection
	 */
	public static synchronized void checkAuth(SessionKey key, GsConnection gsConnection) {
		LoginConnection con = accountsOnLS.get(key.accountId);

		if (con != null && con.getSessionKey().checkSessionKey(key)) {
			/**
			 * account is successful logged in on gs remove it from here
			 */
			accountsOnLS.remove(key.accountId);

			GameServerInfo gsi = gsConnection.getGameServerInfo();
			Account acc = con.getAccount();

			/**
			 * Add account to accounts on GameServer list and update accounts last server
			 */
			gsi.addAccountToGameServer(acc);

			acc.setLastServer(gsi.getId());
			getAccountDAO().updateLastServer(acc.getId(), acc.getLastServer());

			long toll = DAOManager.getDAO(PremiumDAO.class).getPoints(acc.getId());
			/**
			 * Send response to GameServer
			 */
			gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, true, acc.getName(), acc.getCreationDate().getTime(), acc.getAccessLevel(), acc.getMembership(), toll, acc.getAllowedHddSerial()));
		} else {
			gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, false, null, 0, (byte) 0, (byte) 0, 0, null));
		}
	}

	/**
	 * Add account to reconnectionAccount list
	 * 
	 * @param acc
	 */
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

		if (Config.AUTH_EXTERNAL) {
			// authenticate remotely and return received auth state on error
			ExternalAuth auth = ExternalAuthUtil.requestInfo(name, password);

			// if error during auth server connection
			if (auth == null) {
				return AionAuthResponse.STR_L2AUTH_S_ACCOUNTCACHESERVER_DOWN;
			}

			// if received no auth state for account
			if (auth.getAuthState() == null) {
				return AionAuthResponse.STR_L2AUTH_UNKNOWN4;
			}

			AionAuthResponse response = AionAuthResponse.getResponseById(auth.getAuthState());

			// if received invalid auth state
			if (response == null) {
				return AionAuthResponse.STR_L2AUTH_UNKNOWN4;
			}

			switch (response) {
				case STR_L2AUTH_S_ALL_OK:
					// name for this account as sent by external auth server
					accountName = auth.getIdentifier();
					break;
				default:
					// directly return received auth state
					return response;
			}
		}

		// if no or empty account name
		if (StringUtils.isNullOrEmpty(accountName)) {
			return AionAuthResponse.STR_L2AUTH_S_ACCOUNT_LOAD_FAIL;
		}

		Account account = loadAccount(accountName);

		// Try to create new account
		if (account == null && Config.ACCOUNT_AUTO_CREATION) {
			account = createAccount(accountName, password);
		}

		// if account not found and not created
		if (account == null) {
			return AionAuthResponse.STR_L2AUTH_S_ACCOUNT_LOAD_FAIL;
		}

		// if server is under maintenance and account has not the required access level
		if (Config.MAINTENANCE_MOD && account.getAccessLevel() < Config.MAINTENANCE_MOD_GMLEVEL) {
			return AionAuthResponse.STR_L2AUTH_S_SEVER_CHECK;
		}

		// if not external authentication, verify password hash from database
		if (!Config.AUTH_EXTERNAL && !account.getPasswordHash().equals(AccountUtils.encodePassword(password))) {
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
			return AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP;
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
			if (accountsOnLS.containsKey(account.getId())) {
				LoginConnection aionConnection = accountsOnLS.remove(account.getId());

				aionConnection.close(new SM_ACCOUNT_KICK(AionAuthResponse.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN));
				return AionAuthResponse.STR_L2AUTH_S_ALREADY_LOGIN;
			}
			connection.setAccount(account);
			accountsOnLS.put(account.getId(), connection);
		}

		AccountTimeController.updateOnLogin(account);

		// if everything was OK
		getAccountDAO().updateLastIp(account.getId(), connection.getIP());
		// last mac is updated after receiving packet from gameserver
		getAccountDAO().updateMembership(account.getId());

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

			if (accountsOnLS.containsKey(accountId)) {
				LoginConnection conn = accountsOnLS.remove(accountId);
				conn.close(new SM_ACCOUNT_KICK(AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP));
			}
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
		Account account = getAccountDAO().getAccount(name);
		if (account != null) {
			AccountTime accTime = DAOManager.getDAO(AccountTimeDAO.class).getAccountTime(account.getId());
			if (accTime == null)
				throw new NullPointerException("Account Time for account " + account + " is null");
			account.setAccountTime(accTime);
		}
		return account;
	}

	public static Account loadAccount(int id) {
		Account account = getAccountDAO().getAccount(id);
		if (account != null) {
			AccountTime accTime = DAOManager.getDAO(AccountTimeDAO.class).getAccountTime(account.getId());
			if (accTime == null)
				throw new NullPointerException("Account Time for account " + account + " is null");
			account.setAccountTime(accTime);
		}
		return account;
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
		String passwordHash = (!Config.AUTH_EXTERNAL) ? AccountUtils.encodePassword(password) : "";
		Account account = new Account();

		account.setName(name);
		account.setPasswordHash(passwordHash);
		account.setAccessLevel((byte) 0);
		account.setMembership((byte) 0);
		account.setActivated((byte) 1);

		if (getAccountDAO().insertAccount(account)) {
			return account;
		}
		return null;
	}

	/**
	 * Returns {@link com.aionemu.loginserver.dao.AccountDAO}, just a shortcut
	 * 
	 * @return {@link com.aionemu.loginserver.dao.AccountDAO}
	 */
	private static AccountDAO getAccountDAO() {
		return DAOManager.getDAO(AccountDAO.class);
	}

	/**
	 * @param accountId
	 */
	public static synchronized void loadGSCharactersCount(int accountId) {
		GsConnection gsc = null;
		Map<Integer, Integer> accountCharacterCount = null;

		if (accountsGSCharacterCounts.containsKey(accountId))
			accountsGSCharacterCounts.remove(accountId);

		accountsGSCharacterCounts.put(accountId, new HashMap<Integer, Integer>());

		accountCharacterCount = accountsGSCharacterCounts.get(accountId);

		for (GameServerInfo gsi : GameServerTable.getGameServers()) {
			gsc = gsi.getConnection();

			if (gsc != null)
				gsc.sendPacket(new SM_GS_CHARACTER_RESPONSE(accountId));
			else
				accountCharacterCount.put((int) gsi.getId(), 0);
		}

		if (hasAllGSCharacterCounts(accountId))
			sendServerListFor(accountId);
	}

	/**
	 * @param accountId
	 * @return
	 */
	public static synchronized boolean hasAllGSCharacterCounts(int accountId) {
		Map<Integer, Integer> characterCount = accountsGSCharacterCounts.get(accountId);

		if (characterCount != null) {
			if (characterCount.size() == GameServerTable.getGameServers().size())
				return true;
		}

		return false;
	}

	/**
	 * SM_SERVER_LIST call
	 * 
	 * @param accountId
	 */
	public static void sendServerListFor(int accountId) {
		if (accountsOnLS.containsKey(accountId)) {
			accountsOnLS.get(accountId).sendPacket(new SM_SERVER_LIST());
		}
	}

	/**
	 * @param accountId
	 * @return
	 */
	public static Map<Integer, Integer> getGSCharacterCountsFor(int accountId) {
		return accountsGSCharacterCounts.get(accountId);
	}

	/**
	 * @param accountId
	 * @param gsid
	 * @param characterCount
	 */
	public static synchronized void addGSCharacterCountFor(int accountId, int gsid, int characterCount) {
		if (!accountsGSCharacterCounts.containsKey(accountId))
			accountsGSCharacterCounts.put(accountId, new HashMap<Integer, Integer>());

		accountsGSCharacterCounts.get(accountId).put(gsid, characterCount);
	}
}
