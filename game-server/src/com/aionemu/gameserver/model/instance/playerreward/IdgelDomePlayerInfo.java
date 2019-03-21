package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;

/**
 * @author Estrayl
 */
public class IdgelDomePlayerInfo extends InstancePlayerReward {

	private final Race race;
	private int[] reward1;
	private int[] reward2;
	private int[] reward3;
	private int[] reward4;
	private int[] bonusReward;
	private int baseAp;
	private int bonusAp;
	private int baseGp;
	private int bonusGp;

	public IdgelDomePlayerInfo(int objectId, Race race) {
		super(objectId);
		this.race = race;
	}

	public int getReward1ItemId() {
		return reward1 == null ? 0 : reward1[0];
	}

	public int getReward1Count() {
		return reward1 == null ? 0 : reward1[1];
	}

	public int getReward1BonusCount() {
		return reward1 == null ? 0 : reward1[2];
	}

	public void setReward1(int itemId, int count, int bonusCount) {
		reward1 = new int[] { itemId, count, bonusCount };
	}

	public int getReward2ItemId() {
		return reward2 == null ? 0 : reward2[0];
	}

	public int getReward2Count() {
		return reward2 == null ? 0 : reward2[1];
	}

	public int getReward2BonusCount() {
		return reward2 == null ? 0 : reward2[2];
	}

	public void setReward2(int itemId, int count, int bonusCount) {
		reward2 = new int[] { itemId, count, bonusCount };
	}

	public int getReward3ItemId() {
		return reward3 == null ? 0 : reward3[0];
	}

	public int getReward3Count() {
		return reward3 == null ? 0 : reward3[1];
	}

	public void setReward3(int itemId, int count) {
		reward3 = new int[] { itemId, count };
	}

	public int getReward4ItemId() {
		return reward4 == null ? 0 : reward4[0];
	}

	public int getReward4Count() {
		return reward4 == null ? 0 : reward4[1];
	}

	public void setReward4(int itemId, int count) {
		reward4 = new int[] { itemId, count };
	}

	public int getBonusRewardItemId() {
		return bonusReward == null ? 0 : bonusReward[0];
	}

	public int getBonusRewardCount() {
		return bonusReward == null ? 0 : bonusReward[1];
	}

	public void setBonusReward(int itemId, int count) {
		bonusReward = new int[] { itemId, count };
	}

	public int getBaseAp() {
		return baseAp;
	}

	public void setBaseAp(int baseAp) {
		this.baseAp = baseAp;
	}

	public int getBonusAp() {
		return bonusAp;
	}

	public void setBonusAp(int bonusAp) {
		this.bonusAp = bonusAp;
	}

	public int getBaseGp() {
		return baseGp;
	}

	public void setBaseGp(int baseGp) {
		this.baseGp = baseGp;
	}

	public int getBonusGp() {
		return bonusGp;
	}

	public void setBonusGp(int bonusGp) {
		this.bonusGp = bonusGp;
	}

	public Race getRace() {
		return race;
	}
}
