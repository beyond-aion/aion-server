package com.aionemu.gameserver.model.instance.playerreward;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;

/**
 * @author Estrayl
 */
public class IdgelDomePlayerInfo extends InstancePlayerReward {

	private final Race race;
	private final List<RewardItem> itemRewards = new ArrayList<>();
	private int baseAp;
	private int bonusAp;
	private int baseGp;
	private int bonusGp;

	public IdgelDomePlayerInfo(int objectId, Race race) {
		super(objectId);
		this.race = race;
	}

	public List<RewardItem> getItemRewards() {
		return itemRewards;
	}

	public void addItemReward(RewardItem itemReward) {
		itemRewards.add(itemReward);
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
