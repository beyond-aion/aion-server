package com.aionemu.gameserver.model.house;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HouseBidsTest {

	@Test
	void appliesTheRaiseOnlyAfterTheFirstBid() {
		HouseBids bids = new HouseBids(1, 1_000_000, 500_000);
		assertEquals(1_000_000, bids.getMinimumBid());

		bids.restoreBid(2, 1_000_000, 0);
		assertEquals(1_500_000, bids.getMinimumBid());
	}

	@Test
	void allowsTheMaximumBidAsTheNextBid() {
		HouseBids bids = new HouseBids(1, 1_000_000, 100_000);
		bids.restoreBid(2, HouseBids.MAXIMUM_BID - 100_000, 0);

		assertEquals(HouseBids.MAXIMUM_BID, bids.getMinimumBid());
		assertTrue(HouseBids.isValidAuctionAmount(bids.getMinimumBid()));
	}

	@Test
	void aLegacyRaiseBeyondTheLimitLeavesNoValidBid() {
		HouseBids bids = new HouseBids(1, 1_000_000, Long.MAX_VALUE);
		bids.restoreBid(2, 1_000_000, 0);

		assertTrue(bids.getMinimumBid() > HouseBids.MAXIMUM_BID);
		assertFalse(HouseBids.isValidAuctionAmount(bids.getMinimumBid()));
	}
}
