package com.aionemu.commons.network;

/**
 * This class represents ServerCfg for configuring NioServer
 * 
 * @author -Nemesiss-
 * @see com.aionemu.commons.network.ConnectionFactory
 * @see com.aionemu.commons.network.AConnection
 */
public class ServerCfg {

	/**
	 * Host Name on wich we will listen for connections.
	 */
	public final String hostName;
	/**
	 * Port number on wich we will listen for connections.
	 */
	public final int port;
	/**
	 * Connection Name only for logging purposes.
	 */
	public final String connectionName;
	/**
	 * <code>ConnectionFactory</code> that will create <code>AConection</code> object<br>
	 * representing new socket connection.
	 * 
	 * @see com.aionemu.commons.network.ConnectionFactory
	 * @see com.aionemu.commons.network.AConnection
	 */
	public final ConnectionFactory factory;

	/**
	 * Constructor
	 * 
	 * @param hostName
	 *          - Host Name on witch we will listen for connections.
	 * @param port
	 *          - Port number on witch we will listen for connections.
	 * @param connectionName
	 *          - only for logging purposes.
	 * @param factory
	 *          <code>ConnectionFactory</code> that will create <code>AConection</code> object
	 */
	public ServerCfg(String hostName, int port, String connectionName, ConnectionFactory factory) {
		this.hostName = hostName;
		this.port = port;
		this.connectionName = connectionName;
		this.factory = factory;
	}
}
