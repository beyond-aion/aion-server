package com.aionemu.gameserver.model.gameobjects.player.passport;

import java.sql.Timestamp;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;

/**
 * 
 * @author ViAl
 *
 */
public class Passport {

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
	
	public int getRewardStatus() {
		if(this.isFakeStamp())
			return 0;
		else
			return this.rewarded == false ? 1 : 3;
	}
	
	public Timestamp getArriveDate() {
		return arriveDate;
	}

	public void setArriveDate(Timestamp arriveDate) {
		this.arriveDate = arriveDate;
	}

	public PersistentState getState() {
		return state;
	}

	public void setState(PersistentState state) {
		if(this.state == PersistentState.NEW && state != PersistentState.DELETED)
			return;
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
}