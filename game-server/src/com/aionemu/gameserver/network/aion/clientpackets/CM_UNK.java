package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author Rolandas
 */
public class CM_UNK extends AionClientPacket {

	int size;
	int unk0;
	byte unk1;
	byte unk2;
	byte unk3;
	byte unk4;

	int someId;
	int sequence;
	byte[] data;

	public CM_UNK(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		size = readD();
		unk1 = readC();
		unk2 = readC();
		unk3 = readC();
		unk4 = readC();
		size = readD();
		unk0 = readD();
		unk0 = readD();
		someId = readD();
		unk0 = readD();
		sequence = readD();
		unk0 = readD();
		data = readB(size - 36);
	}

	@Override
	protected void runImpl() {

	}

}
