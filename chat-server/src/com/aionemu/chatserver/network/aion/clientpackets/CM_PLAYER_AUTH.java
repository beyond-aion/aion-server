package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.charset.StandardCharsets;

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
	private String identifierSeparator;
	private String accountName;

	public CM_PLAYER_AUTH(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		identifierSeparator = new String(readB(2), StandardCharsets.UTF_16LE); // @
		readC(); // 0
		readD(); // 1
		int gameNameLength = readH() * 2;
		readB(gameNameLength); // AION
		readD(); // 27
		readD(); // 1 or 3
		readD(); // 0
		playerId = readD();
		readD(); // 0
		readD(); // 0
		readD(); // 0
		int length = readH() * 2;
		identifier = readB(length);
		int accountNameLength = readH() * 2;
		accountName = new String(readB(accountNameLength), StandardCharsets.UTF_16LE);
		int tokenLength = readH();
		token = readB(tokenLength);
	}

	@Override
	protected void runImpl() {
		String nameIdentifier = new String(identifier, StandardCharsets.UTF_16LE); // Name@identifier
		String charName = nameIdentifier.substring(0, nameIdentifier.lastIndexOf(identifierSeparator));
		ChatService.getInstance().registerPlayerConnection(playerId, token, identifier, charName, accountName, clientChannelHandler);
	}
}
