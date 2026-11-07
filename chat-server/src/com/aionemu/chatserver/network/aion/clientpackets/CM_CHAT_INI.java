package com.aionemu.chatserver.network.aion.clientpackets;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHAT_INI;

import io.netty.buffer.ByteBuf;

/**
 * @author ginho1
 */
public class CM_CHAT_INI extends AbstractClientPacket {

	private int msgIndex;
	@SuppressWarnings("unused")
	private int versionMajor, versionMinor;

	public CM_CHAT_INI(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		msgIndex = readD();
		versionMajor = readD();
		versionMinor = readD();
	}

	@Override
	protected void runImpl() {
		getConnection().sendPacket(new SM_CHAT_INI(msgIndex));
	}
}
