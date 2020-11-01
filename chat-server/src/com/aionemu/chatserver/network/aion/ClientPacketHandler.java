package com.aionemu.chatserver.network.aion;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.AbstractPacketHandler;
import com.aionemu.chatserver.network.aion.clientpackets.*;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.ClientChannelHandlerState;

/**
 * @author ATracer
 */
public class ClientPacketHandler extends AbstractPacketHandler {

	public AbstractClientPacket handle(ChannelBuffer buf, ClientChannelHandler channelHandler) {
		byte opCode = buf.readByte();
		ClientChannelHandlerState state = channelHandler.getState();
		AbstractClientPacket clientPacket = null;

		switch (state) {
			case CONNECTED:
				switch (opCode) {
					case 0x30 -> clientPacket = new CM_CHAT_INI(buf, channelHandler, opCode);
					case 0x05 -> clientPacket = new CM_PLAYER_AUTH(buf, channelHandler, opCode);
					default -> logUnknownPacket(opCode, state, buf);
				}
				break;
			case AUTHED:
				switch (opCode) {
					case 0x0B -> clientPacket = new CM_CHANNEL_CREATE(buf, channelHandler, opCode);
					case 0x0D -> clientPacket = new CM_CHANNEL_JOIN(buf, channelHandler, opCode);
					case 0x10 -> clientPacket = new CM_CHANNEL_REQUEST(buf, channelHandler, opCode);
					case 0x12 -> clientPacket = new CM_CHANNEL_LEAVE(buf, channelHandler, opCode);
					case 0x18 -> clientPacket = new CM_CHANNEL_MESSAGE(buf, channelHandler, opCode);
					case 0x2C -> clientPacket = new CM_PLAYER_INFO(buf, channelHandler, opCode);
					case (byte) 0xFF -> clientPacket = new CM_PING(buf, channelHandler, opCode);
					default -> logUnknownPacket(opCode, state, buf);
				}
				break;
		}
		return clientPacket;
	}
}
