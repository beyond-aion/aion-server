package com.aionemu.gameserver.network.loginserver;

import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.commons.network.netty.NettyServer;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.AccountTime;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_L2AUTH_LOGIN_CHECK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECONNECT_KEY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;
import com.aionemu.gameserver.network.loginserver.serverpackets.*;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.services.ban.HDDBanService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import io.netty.channel.ChannelFuture;

/**
 * Utility class for connecting GameServer to LoginServer.
 * 
 * @author -Nemesiss-
 */
public class LoginServer {

	private static final Logger log = LoggerFactory.getLogger(LoginServer.class);
	/**
	 * Size of the HDD serial columns of the login server's database. Real serials are at most 20 characters, 82 in the hexadecimal form of
	 * {@code CM_MAC_ADDRESS}.
	 */
	private static final int MAX_HDD_SERIAL_LENGTH = 100;

	/**
	 * The login request of each client that hasn't been authenticated yet. All login request fields are guarded by this instance.
	 */
	private final Map<AionConnection, LoginRequest> loginRequests = new HashMap<>();
	/**
	 * The request of each account that the login server is checking right now. Its answer only names the account, so there is never more than one
	 * request per account at the login server, and the answer goes to the client that sent it.
	 */
	private final Map<Integer, LoginRequest> checkedRequests = new HashMap<>();
	/**
	 * Requests for an account whose previous request is still being checked, in the order they came in.
	 */
	private final Map<Integer, Deque<LoginRequest>> waitingRequests = new HashMap<>();
	private final Map<Integer, AionConnection> loggedInAccounts = new ConcurrentHashMap<>();
	private volatile LoginServerConnection lsCon = null;
	private NettyServer nettyServer = null;
	private int gameServerCount = 1;

	public static LoginServer getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Prevent instantiation.
	 */
	private LoginServer() {
	}

	public void connect(NettyServer nettyServer) {
		if (nettyServer.isShuttingDown()) // a retry that came due after the network was shut down
			return;
		if (lsCon != null)
			throw new IllegalStateException("Login server is already connected.");

		this.nettyServer = nettyServer;
		nettyServer.connect(NetworkConfig.LOGIN_ADDRESS, channel -> {
			lsCon = new LoginServerConnection(channel);
			NettyPipeline.init(channel, lsCon, 0xFFFF);
		}).addListener((ChannelFuture future) -> {
			if (future.isSuccess())
				return;
			lsCon = null;
			int delay;
			if (future.cause() instanceof SocketException) {
				delay = 10;
				log.info("Could not connect to login server at {}, trying again in {} seconds", NetworkConfig.LOGIN_ADDRESS, delay);
			} else {
				delay = 60;
				log.error("Could not connect to login server at {}, trying again in {} seconds", NetworkConfig.LOGIN_ADDRESS, delay, future.cause());
			}
			ThreadPoolManager.getInstance().schedule(() -> connect(nettyServer), delay * 1000);
		});
	}

	/**
	 * When disconnecting we have to close all pending login requests to notify their clients.
	 */
	public void disconnect() {
		if (lsCon != null) {
			lsCon.close();
			lsCon = null;
		}
		synchronized (this) {
			for (AionConnection client : loginRequests.keySet())
				client.close();
			loginRequests.clear();
			checkedRequests.clear();
			waitingRequests.clear();
		}
	}

	public void reconnect() {
		if (lsCon == null)
			return;
		int delay = lsCon.getState() == State.AUTHED ? 5 : 15;
		disconnect();
		log.info("Reconnecting to login server in {} seconds", delay);
		ThreadPoolManager.getInstance().schedule(() -> connect(nettyServer), delay * 1000);
	}

	public boolean isUp() {
		return lsCon != null && lsCon.getState() == State.AUTHED;
	}

	/**
	 * Notify that client is disconnected - we must clear waiting request to LoginServer if any to prevent leaks. Also notify LoginServer that this
	 * account is no longer on GameServer side.
	 */
	public synchronized void onDisconnect(AionConnection connection) {
		LoginRequest loginRequest = loginRequests.remove(connection);
		if (loginRequest != null) {
			Deque<LoginRequest> waiting = waitingRequests.get(loginRequest.accountId());
			if (waiting != null && waiting.remove(loginRequest) && waiting.isEmpty())
				waitingRequests.remove(loginRequest.accountId());
			// a request that the login server is checking stays, its answer has to be handled (see accountAuthenticationResponse)
		}
		// a client that was replaced by a newer login of the same account must not log out the newer one
		Account account = connection.getAccount();
		if (account != null && loggedInAccounts.remove(account.getId(), connection))
			sendPacket(new SM_ACCOUNT_DISCONNECTED(account.getId()));
	}

	public void setGameServerCount(int gameServerCount) {
		this.gameServerCount = gameServerCount;
	}

	public int getGameServerCount() {
		return gameServerCount;
	}

	/**
	 * Remembers the account and session key the client wants to log in with. A client can ask for one account only.
	 */
	public synchronized void registerLoginRequest(int accountId, AionConnection client, int loginOk, int playOk1, int playOk2) {
		if (client.isClosing())
			return;
		if (loginRequests.putIfAbsent(client, new LoginRequest(accountId, client, new SM_ACCOUNT_AUTH(accountId, loginOk, playOk1, playOk2))) != null) {
			log.warn("{} sent a second login request (modified client), disconnecting", client);
			client.close(new SM_L2AUTH_LOGIN_CHECK(false, null));
		}
	}

	/**
	 * Starts authentication procedure of this client - LoginServer will send response with information about account name if authentication is ok.
	 */
	public synchronized void authenticateClient(AionConnection client) {
		LoginRequest loginRequest = loginRequests.get(client);
		if (loginRequest == null || checkedRequests.get(loginRequest.accountId()) == loginRequest)
			return;
		if (!isUp()) {
			loginRequests.remove(client);
			client.close(new SM_L2AUTH_LOGIN_CHECK(false, null)); // disconnect this client since authentication will not happen
		} else if (checkedRequests.containsKey(loginRequest.accountId())) {
			Deque<LoginRequest> waiting = waitingRequests.computeIfAbsent(loginRequest.accountId(), _ -> new ArrayDeque<>());
			if (!waiting.contains(loginRequest))
				waiting.add(loginRequest);
		} else {
			checkedRequests.put(loginRequest.accountId(), loginRequest);
			lsCon.sendPacket(loginRequest.lsAuthRequest());
		}
	}

	/**
	 * Sends the next request waiting for the given account to the login server, if there is one.
	 */
	private void checkNextRequest(int accountId) {
		Deque<LoginRequest> waiting = waitingRequests.get(accountId);
		if (waiting == null)
			return;
		LoginRequest next = waiting.poll();
		if (waiting.isEmpty())
			waitingRequests.remove(accountId);
		if (next == null)
			return;
		if (isUp()) {
			checkedRequests.put(accountId, next);
			lsCon.sendPacket(next.lsAuthRequest());
		} else {
			loginRequests.remove(next.connection());
			next.connection().close(new SM_L2AUTH_LOGIN_CHECK(false, null));
			checkNextRequest(accountId);
		}
	}

	/**
	 * This method is called by CM_ACCOUNT_AUTH_RESPONSE LoginServer packets to notify GameServer about results of client authentication.
	 */
	public synchronized void accountAuthenticationResponse(int accountId, String accountName, boolean result, long creationDate,
		AccountTime accountTime, byte accessLevel, byte membership, String allowedHddSerial) {
		LoginRequest loginRequest = checkedRequests.remove(accountId);
		if (loginRequest == null)
			return;
		AionConnection client = loginRequest.connection();
		loginRequests.remove(client);
		checkNextRequest(accountId);
		if (!result || client.isClosing() || !validateMacAndHddSerial(client, allowedHddSerial)) {
			client.close(new SM_L2AUTH_LOGIN_CHECK(false, accountName)); // LS sends no accName when result is false
			// The login server has moved the account to this game server only if it accepted the session key. Telling it otherwise about an account it
			// didn't accept would log out whoever plays on that account right now.
			if (result)
				sendPacket(new SM_ACCOUNT_DISCONNECTED(accountId));
			return;
		}
		Account account = AccountService.getAccount(accountId, accountName, creationDate, accountTime, accessLevel, membership, allowedHddSerial);
		if (SecurityConfig.HDD_SERIAL_LOCK_UNLOCKED_ACCOUNTS && account.getAllowedHddSerial().isEmpty() && !client.getHddSerial().isEmpty()) {
			account.setAllowedHddSerial(client.getHddSerial());
			sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(account));
		}
		kickOnlineCharacters(account);
		// a client of the same account that is still connected, e.g. at the character selection while the login server link was down
		AionConnection previousClient = loggedInAccounts.put(accountId, client);
		if (previousClient != null)
			previousClient.close(SM_SYSTEM_MESSAGE.STR_KICK_ANOTHER_USER_TRY_LOGIN());
		client.setAccount(account);
		client.setState(AionConnection.State.AUTHED);
		log.info("{} authed with MAC: {} and HDD serial: {}", account, client.getMacAddress(), client.getHddSerial());
		client.sendPacket(new SM_L2AUTH_LOGIN_CHECK(true, accountName));
		sendPacket(new SM_ACCOUNT_CONNECTION_INFO(account.getId(), System.currentTimeMillis(), client.getIP(), client.getMacAddress(), client.getHddSerial()));
	}

	private boolean validateMacAndHddSerial(AionConnection client, String allowedHddSerial) {
		if (!client.getMacAddress().matches("^([0-9A-F]{2}-){5}[0-9A-F]{2}$")) {
			log.warn("{} sent an invalid MAC address (modified client or hack): {}", client, client.getMacAddress());
			return false;
		} else if (client.getHddSerial().length() > MAX_HDD_SERIAL_LENGTH) {
			log.warn("{} sent an HDD serial of {} characters (modified client or hack): {}…", client, client.getHddSerial().length(),
				client.getHddSerial().substring(0, MAX_HDD_SERIAL_LENGTH));
			return false;
		} else if (BannedMacManager.getInstance().isBanned(client.getMacAddress())) {
			log.info("{} was kicked due to mac ban", client);
			return false;
		} else if (HDDBanService.getInstance().isBanned(client.getHddSerial())) {
			log.info("{} was kicked because hdd serial {} is banned", client, client.getHddSerial());
			return false;
		} else if (SecurityConfig.HDD_SERIAL_LOCK_ENABLE && !allowedHddSerial.isEmpty() && !allowedHddSerial.equals(client.getHddSerial())) {
			log.info("{} was kicked due to hdd serial mismatch. Expected {} but client connected with {}", client, allowedHddSerial, client.getHddSerial());
			return false;
		}
		return true;
	}

	private void kickOnlineCharacters(Account account) {
		for (PlayerAccountData accountData : account) {
			PlayerCommonData pcd = accountData.getPlayerCommonData();
			if (pcd.isOnline()) {
				Player player = World.getInstance().getPlayer(pcd.getPlayerObjId());
				if (player != null && player.getClientConnection() != null) {
					player.getClientConnection().close(SM_SYSTEM_MESSAGE.STR_KICK_ANOTHER_USER_TRY_LOGIN()); // kick
				}
			}
		}
	}

	/**
	 * Starts reconnection to LoginServer procedure. LoginServer in response will send reconnection key.
	 */
	public void requestAuthReconnection(int accountId, AionConnection client) {
		if (isUp() && client.equals(loggedInAccounts.get(accountId)))
			lsCon.sendPacket(new SM_ACCOUNT_RECONNECT_KEY(client.getAccount().getId()));
		else
			client.close(/* closePacket */);
	}

	/**
	 * This method is called by CM_ACCOUNT_RECONNECT_KEY LoginServer packets to give GameServer reconnection key for client that was requesting
	 * reconnection.
	 */
	public void authReconnectionResponse(int accountId, int reconnectKey) {
		AionConnection client = loggedInAccounts.get(accountId);
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
			log.info("Kicking account ID {} by LS request", accountId);
			client.close(notifyDoubleLogin ? SM_SYSTEM_MESSAGE.STR_KICK_ANOTHER_USER_TRY_LOGIN() : null);
		}
	}

	public void sendLoggedInAccounts() {
		sendPacket(new SM_ACCOUNT_LIST(new ArrayList<>(loggedInAccounts.values())));
	}

	public void sendLsControlPacket(int type, int param, Player player, Player admin) {
		sendPacket(new SM_LS_CONTROL(type, param, player, admin));
	}

	public AionConnection accountUpdate(int accountId, int type, byte param) {
		AionConnection client = loggedInAccounts.get(accountId);
		if (client != null) {
			Account account = client.getAccount();
			if (type == 1)
				account.setAccessLevel(param);
			else if (type == 2)
				account.setMembership(param);
			return client;
		}
		return null;
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

	private record LoginRequest(int accountId, AionConnection connection, SM_ACCOUNT_AUTH lsAuthRequest) {}
}
