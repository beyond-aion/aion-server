package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
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

	public CM_PLAYER_AUTH(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		identifierSeparator = new String(readB(2), StandardCharsets.UTF_16LE); // @
		readC(); // 0
		readD(); // 1
		int gameNameLength = readUH() * 2;
		readB(gameNameLength); // AION
		readD(); // 27
		readD(); // 1 or 3
		readD(); // 0
		playerId = readD();
		readD(); // 0
		readD(); // 0
		readD(); // 0
		int length = readUH() * 2;
		identifier = readB(length);
		int accountNameLength = readUH() * 2;
		accountName = new String(readB(accountNameLength), StandardCharsets.UTF_16LE);
		int tokenLength = readUH();
		token = readB(tokenLength);
	}

	@Override
	protected void runImpl() {
		String nameIdentifier = new String(identifier, StandardCharsets.UTF_16LE); // Name@identifier
		String charName = nameIdentifier.substring(0, nameIdentifier.lastIndexOf(identifierSeparator));
		ChatService.getInstance().registerPlayerConnection(playerId, token, identifier, charName, accountName, getConnection());
	}
}
