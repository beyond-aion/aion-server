package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * Request to create a private channel
 * 
 * @author Neon
 */
public class CM_CHANNEL_CREATE extends AbstractClientPacket {

	@SuppressWarnings("unused")
	private int channelRequestId;
	@SuppressWarnings("unused")
	private byte[] channelIdentifier, password;

	public CM_CHANNEL_CREATE(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0x40 = @
		readH(); // 0
		channelRequestId = readD();
		readB(16); // 0
		int identifierLength = readH() * 2;
		channelIdentifier = readB(identifierLength);
		readB(7); // 0
		int passwordLength = readH() * 2;
		password = readB(passwordLength);
		readH(); // -1
	}

	@Override
	protected void runImpl() {
		/*
		 * TODO:
		 * - need some way to differentiate between language and "normal" user channels (both have "User" as the type identifier), otherwise users can
		 * create password protected language channels
		 * - rework broadcasting + channels, so each channel has its own user list, to remove unused private channels if the last one leaves it / logs out
		 * - support for private channel password protection
		 */
//		try {
//			System.out.println("Request id: " + channelRequestId);
//			System.out.println("Channel identifier: " + new String(channelIdentifier, StandardCharsets.UTF_16LE));
//			System.out.println("Password: " + new String(password, StandardCharsets.UTF_16LE));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}
}
