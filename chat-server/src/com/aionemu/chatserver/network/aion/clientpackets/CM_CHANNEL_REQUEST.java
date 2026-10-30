package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.charset.StandardCharsets;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.service.ChatService;

import io.netty.buffer.ByteBuf;

/**
 * Request to join a system or language channel (language channels are actually user/private channels)
 * 
 * @author ATracer
 */
public class CM_CHANNEL_REQUEST extends AbstractClientPacket {

	private int channelRequestId;
	private byte[] channelIdentifier;

	public CM_CHANNEL_REQUEST(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		channelRequestId = readD(); // client increases this by 1 for each request (e.g. after teleport)
		readB(16); // user ID, 0
		int length = (readUH() * 2);
		channelIdentifier = readB(length);
		readD(); // 0
	}

	@Override
	protected void runImpl() {
		ChatService.getInstance().registerPlayerWithChannel(getConnection(), channelRequestId,
			new String(channelIdentifier, StandardCharsets.UTF_16LE));
	}
}
