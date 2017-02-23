package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;

/**
 * @author Tibald
 */
public class IronWallFrontPlayerReward extends InstancePlayerReward {

	private Race race;
	private int fragmentedCeramium, ironWarFrontBox, baseReward, bonusReward, gloryPoints;

	public IronWallFrontPlayerReward(int objectId, Race race) {
		super(objectId);
		this.race = race;
	}

	public Race getRace() {
		return race;
	}

	public void setGloryPoints(int gloryPoints) {
		this.gloryPoints = gloryPoints;
	}

	public int getGloryPoints() {
		return gloryPoints;
	}

	public void setFragmentedCeramium(int fragmentedCeramium) {
		this.fragmentedCeramium = fragmentedCeramium;
	}

	public int getFragmentedCeramium() {
		return fragmentedCeramium;
	}

	public void setIronWarFrontBox(int ironWarFrontBox) {
		this.ironWarFrontBox = ironWarFrontBox;
	}

	public int getIronWarFrontBox() {
		return ironWarFrontBox;
	}

	public void setBonusReward(int bonusReward) {
		this.bonusReward = bonusReward;
	}

	public void setBaseReward(int baseReward) {
		this.baseReward = baseReward;
	}

	public int getBonusReward() {
		return bonusReward;
	}

	public int getBaseReward() {
		return baseReward;
	}

}
