package com.aionemu.loginserver.network.aion.clientpackets;

import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

import io.netty.buffer.ByteBuf;

/**
 * This packet is send when client was connected to game server and now is reconnection to login server.
 * 
 * @author -Nemesiss-
 */
public class CM_UPDATE_SESSION extends AionClientPacket {

	/**
	 * accountId is part of session key - its used for security purposes
	 */
	private int accountId;
	/**
	 * loginOk is part of session key - its used for security purposes
	 */
	private int loginOk;
	/**
	 * reconectKey is key that server sends to client for fast reconnection to login server - we will check if this key is valid.
	 */
	private int reconnectKey;

	/**
	 * Constructs new instance of <tt>CM_UPDATE_SESSION </tt> packet.
	 * 
	 * @param buf
	 *          packet data
	 * @param client
	 *          client
	 */
	public CM_UPDATE_SESSION(ByteBuf buf, LoginConnection client, int opCode) {
		super(buf, client, opCode);
	}

	@Override
	protected void readFields() {
		accountId = readD();
		loginOk = readD();
		reconnectKey = readD();
	}

	@Override
	protected void runImpl() {
		AccountController.authReconnectingAccount(accountId, loginOk, reconnectKey, getConnection());
	}
}
