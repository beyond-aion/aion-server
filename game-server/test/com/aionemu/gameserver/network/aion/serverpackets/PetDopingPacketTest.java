package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.model.gameobjects.PetDopingAction.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
		ByteBuf buffer = Unpooled.buffer(12);
		try {
			packet.setBuf(buffer);
			packet.writeImpl(null);

			assertEquals(13, buffer.readShortLE());
			assertEquals(2, buffer.readByte());
			assertEquals(action, buffer.readByte());
			for (int value : values)
				assertEquals(value, buffer.readIntLE());
			assertFalse(buffer.isReadable());
		} finally {
			buffer.release();
		}
	}
}
