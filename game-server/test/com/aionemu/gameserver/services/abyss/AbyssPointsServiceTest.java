package com.aionemu.gameserver.services.abyss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pins the dormant GP loss to the last ranking update and to the GP-only rank.
 */
class AbyssPointsServiceTest {

	private static final long DAY = 24 * 60 * 60;

	@Test
	void countsDormantDaysFromTheRankingUpdate() {
		long rankingUpdate = 1_800_000_000L;
		assertEquals(31L, AbyssPointsService.getDormantDays(rankingUpdate - 31 * DAY, rankingUpdate));
		assertEquals(30L, AbyssPointsService.getDormantDays(rankingUpdate - 30 * DAY - 3600, rankingUpdate));
		assertEquals(0L, AbyssPointsService.getDormantDays(rankingUpdate, rankingUpdate));
		assertEquals(0L, AbyssPointsService.getDormantDays(rankingUpdate + DAY, rankingUpdate));
		assertEquals(0L, AbyssPointsService.getDormantDays(0, rankingUpdate));
	}

	@Test
	void appliesTheDailyLossAfterTheGraceAndCapsAtCurrentGp() {
		assertEquals(0L, AbyssPointsService.getDormantGpLoss(1250, 30, 7));
		assertEquals(7L, AbyssPointsService.getDormantGpLoss(1250, 31, 7));
		assertEquals(14L, AbyssPointsService.getDormantGpLoss(1250, 32, 7));
		assertEquals(1250L, AbyssPointsService.getDormantGpLoss(1250, 1000, 7));
		assertEquals(0L, AbyssPointsService.getDormantGpLoss(1250, 31, 0));
		assertEquals(0L, AbyssPointsService.getDormantGpLoss(0, 31, 7));
	}
}
