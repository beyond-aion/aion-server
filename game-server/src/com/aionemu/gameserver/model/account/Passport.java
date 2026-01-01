package com.aionemu.gameserver.model.account;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;

/**
 * @author ViAl, SVDNESS
 */
public class Passport implements Persistable {
	private int id;
	private boolean rewarded;
	private Timestamp arriveDate;
	private PersistentState state = PersistentState.NOACTION;
	private boolean fakeStamp = false;

	public Passport(int id, boolean rewarded, Timestamp arriveDate) {
		this.id = id;
		this.rewarded = rewarded;
		this.arriveDate = normTs(arriveDate);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isRewarded() {
		return rewarded;
	}

	public void setRewarded(boolean rewarded) {
		this.rewarded = rewarded;
	}

	public RewardStatus getRewardStatus() {
		if (fakeStamp) {
			return rewarded ? RewardStatus.TAKEN : RewardStatus.UPCOMING;
		}
		return rewarded ? RewardStatus.TAKEN : RewardStatus.AVAILABLE;
	}

	public Timestamp getArriveDate() {
		return arriveDate;
	}

	@SuppressWarnings("unused")
	public void setArriveDate(Timestamp arriveDate) {
		this.arriveDate = arriveDate;
	}

	@Override
	public PersistentState getPersistentState() {
		return state;
	}

	@Override
	public void setPersistentState(PersistentState newState) {
		if (newState == null) {
			return;
		}
		if (this.state == PersistentState.NEW && newState == PersistentState.UPDATE_REQUIRED) {
			this.state = PersistentState.UPDATE_REQUIRED;
			return;
		}
		this.state = newState;
	}

	public boolean isFakeStamp() {
		return fakeStamp;
	}

	public void setFakeStamp(boolean fakeStamp) {
		this.fakeStamp = fakeStamp;
	}

	private static Timestamp normTs(Timestamp ts) {
		return ts == null ? null : Timestamp.from(ts.toInstant().truncatedTo(ChronoUnit.SECONDS));
	}

	public AtreianPassport getTemplate() {
		return DataManager.ATREIAN_PASSPORT_DATA.getAtreianPassportId(id);
	}

	public enum RewardStatus {
		UPCOMING(0),
		AVAILABLE(1),
		TAKEN(2),
		EXPIRED(3);

		private final int id;

		RewardStatus(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}
}