package com.aionemu.gameserver.services.abyss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pins the persisted leaderboard update time that the dormant GP loss uses as its reference.
 */
class AbyssRankingCacheTest {

	@Test
	void keepsTheStoredRankingUpdateAcrossRestarts() {
		assertEquals(1794860046, AbyssRankingCache.resolveLastUpdate(1794860046L, 1795000000));
		assertEquals(1795000000, AbyssRankingCache.resolveLastUpdate(null, 1795000000));
		assertEquals(1795000000, AbyssRankingCache.resolveLastUpdate(0L, 1795000000));
	}
}
