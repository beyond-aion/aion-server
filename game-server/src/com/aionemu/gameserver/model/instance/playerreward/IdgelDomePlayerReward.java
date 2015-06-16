package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;

/**
 *
 * @author Ritsu
 */
public class IdgelDomePlayerReward extends InstancePlayerReward 
{

	private Race race;
	private int fragmentedCeramium, idgelDomeBox, baseReward, bonusReward, gloryPoints;

	public IdgelDomePlayerReward(Integer object, Race race) 
	{
		super(object);
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

	public void setIdgelDomeBox(int idgelDomeBox) {
		this.idgelDomeBox = idgelDomeBox;
	}

	public int getIdgelDomeBox() {
		return idgelDomeBox;
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
