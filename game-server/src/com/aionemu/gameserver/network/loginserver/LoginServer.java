package com.aionemu.gameserver.network.loginserver;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.NioServer;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.AccountTime;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_L2AUTH_LOGIN_CHECK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECONNECT_KEY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_AUTH;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_DISCONNECTED;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_LIST;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_RECONNECT_KEY;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_BAN;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_LS_CONTROL;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Utility class for connecting GameServer to LoginServer.
 * 
 * @author -Nemesiss-
 */
public class LoginServer {

	private static final Logger log = LoggerFactory.getLogger(LoginServer.class);

	/**
	 * Map<accountId,Connection> for waiting request. This request is send to LoginServer and GameServer is waiting for response.
	 */
	private Map<Integer, AionConnection> loginRequests = new ConcurrentHashMap<>();

	/**
	 * Map<accountId,Connection> for all logged in accounts.
	 */
	private Map<Integer, AionConnection> loggedInAccounts = new ConcurrentHashMap<>();
	private LoginServerConnection lsCon = null;
	private NioServer nioServer = null;
	private int gameServerCount = 1;

	public static LoginServer getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Prevent instantiation.
	 */
	private LoginServer() {
	}

	public void connect(NioServer nioServer) {
		if (lsCon != null)
			throw new IllegalStateException("LoginServer is already connected.");
		if (nioServer == null)
			throw new NullPointerException("NioServer for LoginServer can not be null.");

		try {
			log.info("Connecting to LoginServer " + NetworkConfig.LOGIN_ADDRESS);
			this.nioServer = nioServer;
			SocketChannel sc = SocketChannel.open(NetworkConfig.LOGIN_ADDRESS);
			sc.configureBlocking(false);
			Dispatcher d = nioServer.getReadWriteDispatcher();
			lsCon = new LoginServerConnection(sc, d);
			d.register(sc, SelectionKey.OP_READ, lsCon);
			lsCon.initialized();
		} catch (IOException e) {
			lsCon = null;
			int delay;
			if (e instanceof SocketException) {
				delay = 10;
				log.info("Could not connect, trying again in " + delay + "s");
			} else {
				delay = 60;
				log.error("Critical error establishing LoginServer socket connection, trying again in " + delay + "s", e);
			}
			ThreadPoolManager.getInstance().schedule(() -> connect(nioServer), delay * 1000);
		}
	}

	/**
	 * When disconnecting we have to close all pending login requests to notify their clients.
	 */
	public void disconnect() {
		if (lsCon != null) {
			lsCon.close();
			lsCon = null;
		}
		for (AionConnection client : loginRequests.values())
			client.close(/* closePacket */); // TODO! some error packet!
		loginRequests.clear();
	}

	public void reconnect() {
		if (lsCon == null)
			return;
		int delay = lsCon.getState() == State.AUTHED ? 5 : 15;
		disconnect();
		log.info("Reconnecting to LoginServer in " + delay + "s...");
		ThreadPoolManager.getInstance().schedule(() -> connect(nioServer), delay * 1000);
	}

	public boolean isUp() {
		return lsCon != null && lsCon.getState() == State.AUTHED;
	}

	/**
	 * Notify that client is disconnected - we must clear waiting request to LoginServer if any to prevent leaks. Also notify LoginServer that this
	 * account is no longer on GameServer side.
	 * 
	 * @param accountId
	 */
	public void aionClientDisconnected(int accountId) {
		loginRequests.remove(accountId);
		loggedInAccounts.remove(accountId);
		sendPacket(new SM_ACCOUNT_DISCONNECTED(accountId));
	}

	public void setGameServerCount(int gameServerCount) {
		this.gameServerCount = gameServerCount;
	}

	public int getGameServerCount() {
		return gameServerCount;
	}

	/**
	 * Starts authentication procedure of this client - LoginServer will send response with information about account name if authentication is ok.
	 * 
	 * @param accountId
	 * @param client
	 * @param loginOk
	 * @param playOk1
	 * @param playOk2
	 */
	public void requestAuthenticationOfClient(int accountId, AionConnection client, int loginOk, int playOk1, int playOk2) {
		if (isUp() && loginRequests.putIfAbsent(accountId, client) == null)
			lsCon.sendPacket(new SM_ACCOUNT_AUTH(accountId, loginOk, playOk1, playOk2));
		else
			client.close(new SM_L2AUTH_LOGIN_CHECK(false, null)); // disconnect this client since authentication will not happen
	}

	/**
	 * This method is called by CM_ACCOUNT_AUTH_RESPONSE LoginServer packets to notify GameServer about results of client authentication.
	 */
	public void accountAuthenticationResponse(int accountId, String accountName, boolean result, long creationDate, AccountTime accountTime,
		byte accessLevel, byte membership, long toll, String allowedHddSerial) {
		AionConnection client = loginRequests.get(accountId);
		if (client == null)
			return;

		if (!result)
			client.close(new SM_L2AUTH_LOGIN_CHECK(false, accountName)); // LS sends no accName when result is false
		else {
			Account account = AccountService.getAccount(accountId, accountName, creationDate, accountTime, accessLevel, membership, toll, allowedHddSerial);
			kickOnlineCharacters(account);
			client.setAccount(account);
			client.setState(AionConnection.State.AUTHED);
			loggedInAccounts.put(accountId, client);
			loginRequests.remove(accountId);
			log.info("Account authed: [Account ID: " + accountId + " Name: " + accountName + "]");
			client.sendPacket(new SM_L2AUTH_LOGIN_CHECK(true, accountName));
		}
	}

	private void kickOnlineCharacters(Account account) {
		for (PlayerAccountData accountData : account) {
			PlayerCommonData pcd = accountData.getPlayerCommonData();
			if (pcd.isOnline()) {
				Player player = World.getInstance().findPlayer(pcd.getPlayerObjId());
				if (player != null && player.getClientConnection() != null) {
					player.getClientConnection().close(SM_SYSTEM_MESSAGE.STR_KICK_ANOTHER_USER_TRY_LOGIN()); // kick
				}
			}
		}
	}

	/**
	 * Starts reconnection to LoginServer procedure. LoginServer in response will send reconnection key.
	 * 
	 * @param accountId
	 * @param client
	 */
	public void requestAuthReconnection(int accountId, AionConnection client) {
		if (isUp() && loggedInAccounts.containsKey(accountId) && loginRequests.putIfAbsent(accountId, client) == null)
			lsCon.sendPacket(new SM_ACCOUNT_RECONNECT_KEY(client.getAccount().getId()));
		else
			client.close(/* closePacket */);
	}

	/**
	 * This method is called by CM_ACCOUNT_RECONNECT_KEY LoginServer packets to give GameServer reconnection key for client that was requesting
	 * reconnection.
	 * 
	 * @param accountId
	 * @param reconnectKey
	 */
	public void authReconnectionResponse(int accountId, int reconnectKey) {
		AionConnection client = loginRequests.remove(accountId);
		if (client == null)
			return;
		client.close(new SM_RECONNECT_KEY(reconnectKey));
	}

	/**
	 * This method is called by CM_REQUEST_KICK_ACCOUNT LoginServer packets to request GameServer to disconnect client with given account id.
	 */
	public void kickAccount(int accountId, boolean notifyDoubleLogin) {
		AionConnection client = loggedInAccounts.get(accountId);
		if (client != null) {
			log.info("Kicking account ID " + accountId + " by LS request.");
			client.close(notifyDoubleLogin ? SM_SYSTEM_MESSAGE.STR_KICK_ANOTHER_USER_TRY_LOGIN() : null);
		}
	}

	public void sendLoggedInAccounts() {
		sendPacket(new SM_ACCOUNT_LIST(new ArrayList<>(loggedInAccounts.values())));
	}

	public void sendLsControlPacket(String accountName, String playerName, String adminName, int param, int type) {
		sendPacket(new SM_LS_CONTROL(accountName, playerName, adminName, param, type));
	}

	public void accountUpdate(int accountId, byte param, int type) {
		AionConnection client = loggedInAccounts.get(accountId);
		if (client != null) {
			Account account = client.getAccount();
			if (type == 1)
				account.setAccessLevel(param);
			else if (type == 2)
				account.setMembership(param);
		}
	}

	public void sendBanPacket(byte type, int accountId, String ip, int time, int adminObjId) {
		sendPacket(new SM_BAN(type, accountId, ip, time, adminObjId));
	}

	public boolean sendPacket(LsServerPacket pk) {
		if (isUp()) {
			lsCon.sendPacket(pk);
			return true;
		} else
			return false;
	}

	private static class SingletonHolder {

		protected static final LoginServer instance = new LoginServer();
	}
}
