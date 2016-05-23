package com.aionemu.chatserver.network.aion;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.AbstractPacketHandler;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_CREATE;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_JOIN;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_LEAVE;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_MESSAGE;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_REQUEST;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHAT_INI;
import com.aionemu.chatserver.network.aion.clientpackets.CM_PING;
import com.aionemu.chatserver.network.aion.clientpackets.CM_PLAYER_AUTH;
import com.aionemu.chatserver.network.aion.clientpackets.CM_PLAYER_INFO;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.State;

/**
 * @author ATracer
 */
public class ClientPacketHandler extends AbstractPacketHandler {

	/**
	 * Reads one packet from ChannelBuffer
	 * 
	 * @param buf
	 * @param channelHandler
	 * @return AbstractClientPacket
	 */
	public AbstractClientPacket handle(ChannelBuffer buf, ClientChannelHandler channelHandler) {
		byte opCode = buf.readByte();
		State state = channelHandler.getState();
		AbstractClientPacket clientPacket = null;

		switch (state) {
			case CONNECTED:
				switch (opCode) {
					case 0x30:
						clientPacket = new CM_CHAT_INI(buf, channelHandler, opCode);
						break;
					case 0x05:
						clientPacket = new CM_PLAYER_AUTH(buf, channelHandler, opCode);
						break;
					default:
						unknownPacket(opCode, state, buf);
						break;
				}
				break;
			case AUTHED:
				switch (opCode) {
					case 0x0B:
						clientPacket = new CM_CHANNEL_CREATE(buf, channelHandler, opCode);
						break;
					case 0x0D:
						clientPacket = new CM_CHANNEL_JOIN(buf, channelHandler, opCode);
						break;
					case 0x10:
						clientPacket = new CM_CHANNEL_REQUEST(buf, channelHandler, opCode);
						break;
					case 0x12:
						clientPacket = new CM_CHANNEL_LEAVE(buf, channelHandler, opCode);
						break;
					case 0x18:
						clientPacket = new CM_CHANNEL_MESSAGE(buf, channelHandler, opCode);
						break;
					case 0x2C:
						clientPacket = new CM_PLAYER_INFO(buf, channelHandler, opCode);
						break;
					case (byte) 0xFF:
						clientPacket = new CM_PING(buf, channelHandler, opCode);
						break;
					default:
						unknownPacket(opCode, state, buf);
						break;
				}
				break;
		}

		return clientPacket;
	}
}
