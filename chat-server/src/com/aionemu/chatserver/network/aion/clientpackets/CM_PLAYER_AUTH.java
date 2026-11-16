package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.charset.StandardCharsets;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.service.ChatService;

import io.netty.buffer.ByteBuf;

/**
 * @author ATracer
 */
public class CM_PLAYER_AUTH extends AbstractClientPacket {

	private int msgIndex;
	private int playerId;
	private byte[] token;
	private byte[] identifier;
	private String accountName;

	public CM_PLAYER_AUTH(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		msgIndex = readD(); // 1
		int gameNameLength = readUH() * 2;
		readB(gameNameLength); // AION
		readD(); // 27
		readQ(); // first half of the user ID, 1 or 3
		playerId = readD(); // second half of the user ID (Q), the game user ID
		readD();
		readQ(); // 0
		int length = readUH() * 2;
		identifier = readB(length); // name@server
		int accountNameLength = readUH() * 2;
		accountName = new String(readB(accountNameLength), StandardCharsets.UTF_16LE);
		int tokenLength = readUH();
		token = readB(tokenLength);
	}

	@Override
	protected void runImpl() {
		String nameIdentifier = new String(identifier, StandardCharsets.UTF_16LE); // Name@identifier
		String charName = nameIdentifier.substring(0, nameIdentifier.lastIndexOf('@'));
		ChatService.getInstance().registerPlayerConnection(msgIndex, playerId, token, identifier, charName, accountName, getConnection());
	}
}
