package com.aionemu.chatserver.network.aion.clientpackets;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_PLAYER_AUTH extends AbstractClientPacket {

	private ChatService chatService;
	private int playerId;
	private byte[] token;
	private byte[] identifier;
	@SuppressWarnings("unused")
	private byte[] accountName;
	private String realName;

	/**
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_PLAYER_AUTH(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, ChatService chatService) {
		super(channelBuffer, clientChannelHandler, 0x05);
		this.chatService = chatService;
	}

	@Override
	protected void readImpl() {
		readB(29); // AION stuff
		this.playerId = readD();
		readD(); // 0x00
		readD(); // 0x00
		readD(); // 0x00
		int length = readH() * 2;
		identifier = readB(length);
		int accountLenght = readH() * 2;
		accountName = readB(accountLenght);
		int tokenLength = readH();
		token = readB(tokenLength);

		try {
			String realid = new String(identifier, "UTF-16le");
			realName = realid.split("@")[0];
			String after = realid.split("@")[1];
			identifier = after.getBytes("UTF-16le");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void runImpl() {
		try {
			chatService.registerPlayerConnection(playerId, token, identifier, clientChannelHandler, realName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
