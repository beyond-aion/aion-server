package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.model.gameobjects.PetDopingAction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

class PetDopingPacketTest {

	@Test
	void serializesSuccessfulActions() {
		assertDopingBody(SM_PET.doping(ADD, 123, 2), 0, 123, 2);
		assertDopingBody(SM_PET.doping(REMOVE, 123, 2), 1, 2);
		assertDopingBody(SM_PET.dopingSwitched(2, 3), 2, 2, 3);
		assertDopingBody(SM_PET.doping(USE, 123, 2), 3, 123);
	}

	@Test
	void serializesRejectedActions() {
		assertDopingBody(SM_PET.dopingRejected(ADD), 0, -1, -1);
		assertDopingBody(SM_PET.dopingRejected(REMOVE), 1, -1);
		assertDopingBody(SM_PET.dopingRejected(SWITCH), 2, -1, -1);
		assertDopingBody(SM_PET.dopingRejected(USE), 3, -1);
	}

	private static void assertDopingBody(SM_PET packet, int action, int... values) {
		ByteBuffer buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
		packet.setBuf(buffer);
		packet.writeImpl(null);
		buffer.flip();

		assertEquals(13, buffer.getShort());
		assertEquals(2, buffer.get());
		assertEquals(action, buffer.get());
		for (int value : values)
			assertEquals(value, buffer.getInt());
		assertFalse(buffer.hasRemaining());
	}
}
