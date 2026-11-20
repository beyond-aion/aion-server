package com.aionemu.gameserver.controllers.attack;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Pins the expiry of an attacker's damage.
 */
class AggroInfoTest {

	@Test
	void dropsTheDamageOfAnAttackerThatStoppedHitting() {
		AggroInfo info = new AggroInfo(null);
		info.addDamage(500, 1_000);
		info.addDamage(300, 61_000);
		assertFalse(info.isDamageExpired(180_999, 120_000));
		assertTrue(info.isDamageExpired(181_000, 120_000));
		info.expireDamage(181_000, 120_000);
		assertEquals(0, info.getDamage());
		// the next hit starts over, also as the first damage
		info.addDamage(100, 200_000);
		assertEquals(100, info.getDamage());
		assertEquals(200_000, info.getFirstDamageTime());
	}

	@Test
	void keepsTheLastHitOfTransferredDamage() {
		AggroInfo master = new AggroInfo(null);
		master.addDamage(400, 1_000, 90_000);
		assertEquals(1_000, master.getFirstDamageTime());
		assertEquals(90_000, master.getLastDamageTime());
		assertFalse(master.isDamageExpired(200_000, 120_000));
	}
}
