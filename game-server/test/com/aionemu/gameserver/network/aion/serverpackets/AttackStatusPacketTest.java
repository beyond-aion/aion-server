package com.aionemu.gameserver.network.aion.serverpackets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;

class AttackStatusPacketTest {

	@Test
	void flightPointDamageIsNegativeAndRingRecoveryIsPositive() {
		Creature target = new Creature(123, null, null, new NpcTemplate(), null, false) {

			@Override
			public byte getLevel() {
				return 1;
			}
		};
		target.setLifeStats(new CreatureLifeStats<>(target) {

			@Override
			public int getMaxHp() {
				return 100;
			}

			@Override
			public int getMaxMp() {
				return 100;
			}
		});

		assertFlightPointChange(target, TYPE.FP_DAMAGE_INSTANT, 24, -7);
		assertFlightPointChange(target, TYPE.DELAYED_FP_DAMAGE, 28, -7);
		assertFlightPointChange(target, TYPE.FP_RINGS, 24, 7);
	}

	private static void assertFlightPointChange(Creature target, TYPE type, int expectedType, int expectedValue) {
		SM_ATTACK_STATUS packet = new SM_ATTACK_STATUS(target, type, 0, 7);
		ByteBuffer body = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN);
		packet.setBuf(body);
		packet.writeImpl(null);
		assertEquals(expectedValue, body.getInt(4));
		assertEquals(expectedType, Byte.toUnsignedInt(body.get(8)));
		assertEquals(100, Byte.toUnsignedInt(body.get(9)));
	}
}
