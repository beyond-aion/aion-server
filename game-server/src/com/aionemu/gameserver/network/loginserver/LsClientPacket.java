package com.aionemu.gameserver.network.loginserver;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

/**
 * @author -Nemesiss-
 */
public abstract class LsClientPacket extends BaseClientPacket<LoginServerConnection> {

	/**
	 * Constructs new client packet with specified opcode. If using this constructor, user must later manually set buffer and connection.
	 * 
	 * @param opcode
	 *          packet id
	 */
	protected LsClientPacket(int opcode) {
		super(opcode);
	}

	/**
	 * run runImpl catching and logging Throwable.
	 */
	@Override
	public final void run() {
		try {
			runImpl();
		} catch (Throwable e) {
			LoggerFactory.getLogger(LsClientPacket.class).error("Error handling LS packet from " + getConnection().getIP() + ": " + this, e);
		}
	}

	/**
	 * Send new LsServerPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
	 * 
	 * @param msg
	 */
	protected void sendPacket(LsServerPacket msg) {
		getConnection().sendPacket(msg);
	}

}
