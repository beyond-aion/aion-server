package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author Neon
 */
public class CM_PLAYER_INFO extends AbstractClientPacket {

	@SuppressWarnings("unused")
	private int classId, level;
	@SuppressWarnings("unused")
	private byte[] unk;

	/**
	 * Client sends this after authentication and after each teleport.
	 */
	public CM_PLAYER_INFO(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0
		readH(); // 0
		classId = readC();
		readD(); // 0
		level = readD();
		unk = readB(135);
	}

	@Override
	protected void runImpl() {
		// TODO Find out what other information is sent, maybe handle it if it's useful
	}
}
