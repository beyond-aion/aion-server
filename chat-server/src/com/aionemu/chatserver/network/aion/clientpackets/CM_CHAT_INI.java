package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHAT_INI;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ginho1
 */
public class CM_CHAT_INI extends AbstractClientPacket {

	public CM_CHAT_INI(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC();
		readH();
		readD();
		readD();
		readD();
	}

	@Override
	protected void runImpl() {
		clientChannelHandler.sendPacket(new SM_CHAT_INI());
	}
}
