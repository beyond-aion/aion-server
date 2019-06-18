package com.aionemu.gameserver.taskmanager.tasks.housing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.taskmanager.AbstractCronTask;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Handles housing auction end and potential prolongations if there are new bids just before auction end.
 * 
 * @author Neon
 */
public class AuctionEndTask extends AbstractCronTask {

	private static final long PROLONGATION_MILLIS = TimeUnit.MINUTES.toMillis(5);
	private static final long MAX_PROLONGATION_MILLIS = TimeUnit.MINUTES.toMillis(30);
	private static final AuctionEndTask instance = new AuctionEndTask();
	private Map<Integer, ProlongedAuction> prolongedAuctions = new ConcurrentHashMap<>();

	public static AuctionEndTask getInstance() {
		return instance;
	}

	private AuctionEndTask() {
		super(HousingConfig.HOUSE_AUCTION_END_TIME);
	}

	@Override
	protected boolean shouldRunOnStart() {
		if (super.shouldRunOnStart()) // true if the server was down when auctions should have ended (SERVER_STOP_MILLIS < lastPlannedRun)
			return true;
		if (SERVER_STOP_MILLIS != null) // trigger auction end if the server shut down in the prolongation time frame (30min after regular auction end)
			return SERVER_STOP_MILLIS - getLastPlannedRun().getTime() <= MAX_PROLONGATION_MILLIS;
		return false;
	}

	@Override
	protected void executeTask() {
		HousingBidService.getInstance().endAuctions();
	}

	public int getRemainingAuctionSeconds(int houseObjectId) {
		ProlongedAuction prolongedAuction = prolongedAuctions.get(houseObjectId);
		long auctionEndMillis = prolongedAuction == null ? getNextRun().getTime() : prolongedAuction.auctionEndMillis;
		return (int) ((auctionEndMillis - System.currentTimeMillis()) / 1000);
	}

	public void onAuctionEnd(int houseObjectId) {
		ProlongedAuction prolongedAuction = prolongedAuctions.remove(houseObjectId);
		if (prolongedAuction != null)
			prolongedAuction.task.cancel(false);
	}

	/**
	 * @return True if the auction did not need to be prolonged or was prolonged successfully. False if the auction just ended.
	 */
	public boolean tryProlongAuction(int houseObjectId) {
		long millisUntilAuctionEnd = getMillisUntilNextRun();
		long millisSinceLastAuctionEnd = getMillisSinceLastRun();
		long delayMillis = 0;
		if (millisUntilAuctionEnd <= TimeUnit.MINUTES.toMillis(5)) // initial extension is 5 minutes after regular auction end
			delayMillis = millisUntilAuctionEnd + PROLONGATION_MILLIS;
		else if (millisSinceLastAuctionEnd != -1 && millisSinceLastAuctionEnd < MAX_PROLONGATION_MILLIS) // max extension is 30 minutes
			delayMillis = Math.min(TimeUnit.MINUTES.toMillis(30) - millisSinceLastAuctionEnd, PROLONGATION_MILLIS);
		return delayMillis == 0 || prolongAuction(houseObjectId, delayMillis);
	}

	public boolean isAuctionProlonged(int houseObjectId) {
		return prolongedAuctions.containsKey(houseObjectId);
	}

	/**
	 * @return True if the auction could be prolonged. False if it just ended.
	 */
	private boolean prolongAuction(int houseObjectId, long delayMillis) {
		ProlongedAuction prolongedAuction = prolongedAuctions.compute(houseObjectId, (key, oldValue) -> {
			if (oldValue == null)
				oldValue = new ProlongedAuction(houseObjectId, delayMillis);
			else if (!oldValue.prolong(delayMillis))
				return null;
			return oldValue;
		});
		return prolongedAuction != null;
	}

	private class ProlongedAuction {

		private final int houseObjectId;
		private long auctionEndMillis;
		private Future<?> task;

		private ProlongedAuction(int houseObjectId, long delayMillis) {
			this.houseObjectId = houseObjectId;
			prolong(delayMillis);
		}

		private boolean prolong(long delayMillis) {
			if (task != null && !task.cancel(false))
				return false;
			auctionEndMillis = System.currentTimeMillis() + delayMillis;
			task = ThreadPoolManager.getInstance().schedule(() -> HousingBidService.getInstance().endAuction(houseObjectId), delayMillis);
			return true;
		}
	}
}
