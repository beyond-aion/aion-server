package com.aionemu.loginserver;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection.State;

/**
 * This class represents GameServer at LoginServer side. It contain info about id, ip etc.
 * 
 * @author -Nemesiss-
 */
public class GameServerInfo {

	/**
	 * Id of this GameServer
	 */
	private final byte id;

	/**
	 * Allowed IP for this GameServer if gs will connect from another ip wont be registered.
	 */
	private final String ipMask;

	/**
	 * Password
	 */
	private final String password;

	/**
	 * Default server address, usually internet address
	 */
	private byte[] ip = { 0, 0, 0, 0 };

	/**
	 * Port on with this GameServer is accepting clients.
	 */
	private int port;

	/**
	 * gsConnection - if GameServer is connected to LoginServer.
	 */
	private GsConnection gscHandler;

	/**
	 * minimum access level to be able to connect to this game server
	 */
	private byte minAccessLevel;

	/**
	 * Max players count that may play on this GameServer.
	 */
	private int maxPlayers;

	/**
	 * Map<AccId,Account> of accounts logged in on this GameServer.
	 */
	private final Map<Integer, Account> accountsOnGameServer = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param ip
	 * @param password
	 */
	public GameServerInfo(byte id, String ipMask, String password) {
		this.id = id;
		this.ipMask = ipMask;
		this.password = password;
	}

	/**
	 * Returns id of this GameServer.
	 * 
	 * @return byte id
	 */
	public byte getId() {
		return id;
	}

	/**
	 * Returns Password of this GameServer.
	 * 
	 * @return String password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns allowed IP for this GameServer.
	 * 
	 * @return String ip
	 */
	public String getIpMask() {
		return ipMask;
	}

	/**
	 * Returns port of this GameServer.
	 * 
	 * @return in port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set port for this GameServer.
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns default server address, usually used as Internet address
	 * 
	 * @return default server address
	 */
	public byte[] getIp() {
		return ip;
	}

	/**
	 * Sets default server address
	 * 
	 * @param ip
	 *          default server address
	 */
	public void setIp(byte[] ip) {
		this.ip = ip;
	}

	/**
	 * Returns active GsConnection for this GameServer or null if this GameServer is down.
	 * 
	 * @return GsConnection
	 */
	public final GsConnection getConnection() {
		return gscHandler;
	}

	/**
	 * Set active GsConnection.
	 * 
	 * @param gsConnection
	 */
	public final void setConnection(GsConnection gscHandler) {
		this.gscHandler = gscHandler;
	}

	public byte getMinAccessLevel() {
		return minAccessLevel;
	}

	public void setMinAccessLevel(byte minAccessLevel) {
		this.minAccessLevel = minAccessLevel;
	}

	/**
	 * Returns number of max allowed players for this GameServer.
	 * 
	 * @return int maxPlayers
	 */
	public final int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * Set max allowed players for this GameServer.
	 * 
	 * @param maxPlayers
	 */
	public final void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	/**
	 * Check if GameServer is Online
	 * 
	 * @return true if GameServer is Online.
	 */
	public final boolean isOnline() {
		return gscHandler != null && gscHandler.getState() == State.AUTHED;
	}

	/**
	 * Check if given account is already on This GameServer
	 * 
	 * @param accountId
	 * @return true if account is on this GameServer
	 */
	public final boolean isAccountOnGameServer(int accountId) {
		return accountsOnGameServer.containsKey(accountId);
	}

	/**
	 * Remove account from this GameServer
	 * 
	 * @param accountId
	 * @return removed account.
	 */
	public final Account removeAccountFromGameServer(int accountId) {
		return accountsOnGameServer.remove(accountId);
	}

	/**
	 * Add account to this GameServer
	 * 
	 * @param acc
	 */
	public final void addAccountToGameServer(Account acc) {
		accountsOnGameServer.put(acc.getId(), acc);
	}

	/**
	 * Get Account object from account on GameServer list.
	 * 
	 * @param accountId
	 * @return Account object if account is on this game server or null.
	 */
	public final Account getAccountFromGameServer(int accountId) {
		return accountsOnGameServer.get(accountId);
	}

	/**
	 * Clears all accounts on this gameServer
	 */
	public void clearAccountsOnGameServer() {
		accountsOnGameServer.clear();
	}

	/**
	 * Return number of online players connected to this GameServer.
	 * 
	 * @return number of online players
	 */
	public int getCurrentPlayers() {
		return accountsOnGameServer.size();
	}

	/**
	 * Return true if server is full.
	 * 
	 * @return true if full.
	 */
	public boolean isFull() {
		return getCurrentPlayers() >= getMaxPlayers();
	}
}
