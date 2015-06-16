package com.aionemu.gameserver.services.mail;

/**
 * @author Rolandas
 */
public enum AuctionResult {
	FAILED_BID(0),
	CANCELED_BID(1),
	FAILED_SALE(2),
	SUCCESS_SALE(3),
	WIN_BID(4),
	GRACE_START(5),
	GRACE_FAIL(6),
	GRACE_SUCCESS(7);

	private int value;

	private AuctionResult(int value) {
		this.value = value;
	}

	public int getId() {
		return this.value;
	}

	public static AuctionResult getResultFromId(int resultId) {
		for (AuctionResult result : AuctionResult.values()) {
			if (result.getId() == resultId)
				return result;
		}
		return null;
	}
}
