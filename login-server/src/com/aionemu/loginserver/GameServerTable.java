package com.aionemu.loginserver;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.loginserver.dao.GameServersDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsAuthResponse;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_REQUEST_KICK_ACCOUNT;

/**
 * GameServerTable contains list of GameServers registered on this LoginServer. GameServer may by online or down.
 * 
 * @author -Nemesiss-
 */
public class GameServerTable {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(GameServerTable.class);

	/**
	 * Map<Id,GameServer>
	 */
	private static Map<Byte, GameServerInfo> gameservers;

	/**
	 * Return collection contains all registered [up/down] GameServers.
	 * 
	 * @return collection of GameServers.
	 */
	public static Collection<GameServerInfo> getGameServers() {
		return Collections.unmodifiableCollection(gameservers.values());
	}

	/**
	 * Load GameServers from database.
	 */
	public static void load() {
		gameservers = getDAO().getAllGameServers();
		log.info("GameServerTable loaded " + gameservers.size() + " registered GameServers.");
	}

	/**
	 * Register GameServer if its possible.
	 * 
	 * @param gsConnection
	 *          Connection object
	 * @param requestedId
	 *          id of server that was requested
	 * @param ip
	 *          default network address from server, usually internet address
	 * @param port
	 *          port that is used by server
	 * @param maxPlayers
	 *          maximum amount of players
	 * @param password
	 *          server password that is specified configs, used to check if gs can auth on ls
	 * @return GsAuthResponse
	 */
	public static GsAuthResponse registerGameServer(GsConnection gsConnection, byte requestedId, byte[] ip, int port, int maxPlayers, String password) {
		GameServerInfo gsi = gameservers.get(requestedId);

		/**
		 * This id is not Registered at LoginServer.
		 */
		if (gsi == null) {
			log.warn(gsConnection + " requestedID: " + requestedId + " is not registered in LS database!");
			return GsAuthResponse.NOT_AUTHED;
		}

		/**
		 * Check if this GameServer is not already registered.
		 */
		if (gsi.getConnection() != null)
			return GsAuthResponse.ALREADY_REGISTERED;

		/**
		 * Check if password and ip are ok.
		 */
		if (!gsi.getPassword().equals(password) || !NetworkUtils.checkIPMatching(gsi.getIpMask(), gsConnection.getIP())) {
			log.warn(gsConnection + " requested ID: " + requestedId + " has wrong IP or password!");
			return GsAuthResponse.NOT_AUTHED;
		}

		gsi.setIp(ip);
		gsi.setPort(port);
		gsi.setMaxPlayers(maxPlayers);
		gsi.setConnection(gsConnection);

		gsConnection.setGameServerInfo(gsi);
		return GsAuthResponse.AUTHED;
	}

	/**
	 * Returns GameSererInfo object for given gameserverId.
	 * 
	 * @param gameServerId
	 * @return GameSererInfo object for given gameserverId.
	 */
	public static GameServerInfo getGameServerInfo(byte gameServerId) {
		return gameservers.get(gameServerId);
	}

	/**
	 * Check if account is already in use on any GameServer. If so - kick account from GameServer.
	 * 
	 * @param acc
	 *          account to check
	 * @return true is account is logged in on one of GameServers
	 */
	public static boolean isAccountOnAnyGameServer(Account acc) {
		for (GameServerInfo gsi : getGameServers()) {
			if (gsi.isAccountOnGameServer(acc.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper method, used to kick account from any gameServer if it's logged in
	 * 
	 * @param accountId
	 *          - account that must be kicked at GameServer side
	 * @param notifyDoubleLogin
	 *          - whether to notify the player that he got kicked due to another client logging in
	 * @return True, if account was kicked. False, if he was not on any gameserver.
	 */
	public static boolean kickAccountFromGameServer(int accountId, boolean notifyDoubleLogin) {
		for (GameServerInfo gsi : getGameServers()) {
			if (gsi.isAccountOnGameServer(accountId)) {
				gsi.getConnection().sendPacket(new SM_REQUEST_KICK_ACCOUNT(accountId, notifyDoubleLogin));
				return true;
			}
		}
		return false;
	}

	/**
	 * Retuns {@link com.aionemu.loginserver.dao.GameServersDAO} , just a shortcut
	 * 
	 * @return {@link com.aionemu.loginserver.dao.GameServersDAO}
	 */
	private static GameServersDAO getDAO() {
		return DAOManager.getDAO(GameServersDAO.class);
	}

	public static void pong(byte serverId, int pid) {
		for (GameServerInfo gsi : getGameServers()) {
			if (gsi.getId() == serverId) {
				gsi.getConnection().pong(pid);
				break;
			}
		}
	}
}
