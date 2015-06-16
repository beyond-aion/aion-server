package com.aionemu.gameserver.network;

/**
 * 
 * @author KID
 *
 */
public class NetworkController {

	private static NetworkController instance = new NetworkController();
	
	public static NetworkController getInstance()
	{
		return instance;
	}
	
	private byte serverCount = 1;
	
	public final byte getServerCount() {
		return this.serverCount;
	}
	
	public final void setServerCount(byte count) {
		this.serverCount = count;
	}
}
