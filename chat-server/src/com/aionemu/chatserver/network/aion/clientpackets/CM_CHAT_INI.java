package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHAT_INI;

/**
 * @author ginho1
 */
public class CM_CHAT_INI extends AbstractClientPacket {

	public CM_CHAT_INI(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readUC();
		readUH();
		readD();
		readD();
		readD();
	}

	@Override
	protected void runImpl() {
		getConnection().sendPacket(new SM_CHAT_INI());
	}
}
