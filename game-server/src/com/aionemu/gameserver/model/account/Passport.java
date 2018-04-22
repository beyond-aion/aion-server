package com.aionemu.gameserver.model.account;

import java.sql.Timestamp;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;

/**
 * @author ViAl
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
		this.arriveDate = arriveDate;
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
		if (fakeStamp)
			return rewarded ? RewardStatus.TAKEN : RewardStatus.UPCOMING;
		else
			return rewarded ? RewardStatus.EXPIRED : RewardStatus.AVAILABLE;
	}

	public Timestamp getArriveDate() {
		return arriveDate;
	}

	public void setArriveDate(Timestamp arriveDate) {
		this.arriveDate = arriveDate;
	}

	@Override
	public PersistentState getPersistentState() {
		return state;
	}

	@Override
	public void setPersistentState(PersistentState state) {
		if (this.state == PersistentState.NEW) {
			if (state == PersistentState.UPDATE_REQUIRED)
				return;
			else if (state == PersistentState.DELETED) {
				this.state = PersistentState.NOACTION;
				return;
			}
		}
		this.state = state;
	}

	public boolean isFakeStamp() {
		return fakeStamp;
	}

	public void setFakeStamp(boolean fakeStamp) {
		this.fakeStamp = fakeStamp;
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
