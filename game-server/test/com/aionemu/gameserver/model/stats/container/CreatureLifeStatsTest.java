package com.aionemu.gameserver.model.stats.container;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * Tests for HP percentage calculation in CreatureLifeStats.
 * Validates fix for issue #28: HP showing 0% for living creatures.
 */
class CreatureLifeStatsTest {

	/**
	 * Minimal test implementation of CreatureLifeStats that allows setting maxHp directly.
	 */
	private static class TestLifeStats extends CreatureLifeStats<Creature> {

		private final int maxHp;

		TestLifeStats(int currentHp, int maxHp) {
			super(null, currentHp, 0);
			this.maxHp = maxHp;
		}

		@Override
		public int getMaxHp() {
			return maxHp;
		}

		@Override
		public int getMaxMp() {
			return 100;
		}
	}

	@Test
	void testHpPercentageReturnsZeroWhenDead() {
		TestLifeStats stats = new TestLifeStats(0, 1000);
		assertEquals(0, stats.getHpPercentage(), "Dead creature (HP=0) should return 0%");
	}

	@Test
	void testHpPercentageReturnsMinOneWhenAliveWithVeryLowHp() {
		TestLifeStats stats = new TestLifeStats(1, 10000);
		assertEquals(1, stats.getHpPercentage(), "Living creature with <1% HP should return 1%, not 0%");
	}

	@Test
	void testHpPercentageReturnsMinOneAtExactlyHalfPercent() {
		TestLifeStats stats = new TestLifeStats(5, 1000);
		assertEquals(1, stats.getHpPercentage(), "0.5% HP should round up to 1%");
	}

	@Test
	void testHpPercentageReturnsHundredWhenFull() {
		TestLifeStats stats = new TestLifeStats(1000, 1000);
		assertEquals(100, stats.getHpPercentage(), "Full HP should return 100%");
	}

	@Test
	void testHpPercentageCalculatesCorrectlyAtFiftyPercent() {
		TestLifeStats stats = new TestLifeStats(500, 1000);
		assertEquals(50, stats.getHpPercentage(), "50% HP should return 50");
	}

	@Test
	void testHpPercentageCalculatesCorrectlyAtOnePercent() {
		TestLifeStats stats = new TestLifeStats(10, 1000);
		assertEquals(1, stats.getHpPercentage(), "1% HP should return 1");
	}

	@Test
	void testHpPercentageCalculatesCorrectlyAtNinetyNinePercent() {
		TestLifeStats stats = new TestLifeStats(990, 1000);
		assertEquals(99, stats.getHpPercentage(), "99% HP should return 99");
	}
}
