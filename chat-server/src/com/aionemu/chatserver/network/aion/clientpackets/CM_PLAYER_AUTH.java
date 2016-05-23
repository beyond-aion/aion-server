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

	private int playerId;
	private byte[] token;
	private byte[] identifier;
	private byte[] accountName;

	public CM_PLAYER_AUTH(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0x40 = @
		readH(); // 0
		readD(); // 1
		int gameNameLength = readH() * 2;
		readB(gameNameLength); // AION
		readD(); // 27
		int unkLength = readH() * 2;
		readB(unkLength); // empty
		playerId = readD();
		readD(); // 0
		readD(); // 0
		readD(); // 0
		int length = readH() * 2;
		identifier = readB(length); // Name@identifier
		int accountLenght = readH() * 2;
		accountName = readB(accountLenght);
		int tokenLength = readH();
		token = readB(tokenLength);
	}

	@Override
	protected void runImpl() {
		try {
			String nameIdentifier = new String(identifier, "UTF-16le");
			String charName = nameIdentifier.substring(0, nameIdentifier.lastIndexOf("@"));
			String accName = new String(accountName, "UTF-16le");
			ChatService.getInstance().registerPlayerConnection(playerId, token, identifier, charName, accName, clientChannelHandler);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
