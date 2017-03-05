package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceBuff;

/**
 * @author xTz
 */
public class KamarPlayerReward extends InstancePlayerReward {

	private Race race;
	private int kamarBox, bloodMarks, baseReward, bonusReward, gloryPoints;
	private InstanceBuff boostMorale;

	public KamarPlayerReward(int objectId, Race race) {
		super(objectId);
		this.race = race;
		boostMorale = new InstanceBuff(10);
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

	public void setKamarBox(int kamarBox) {
		this.kamarBox = kamarBox;
	}

	public int getKamarBox() {
		return kamarBox;
	}

	public void setBloodMarks(int bloodMarks) {
		this.bloodMarks = bloodMarks;
	}

	public int getBloodMarks() {
		return bloodMarks;
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

	public boolean hasBoostMorale() {
		return boostMorale.hasInstanceBuff();
	}

	public void applyBoostMoraleEffect(Player player) {
		boostMorale.applyEffect(player, 30000);
	}

	public void endBoostMoraleEffect(Player player) {
		boostMorale.endEffect(player);
	}

	public int getRemaningTime() {
		int time = boostMorale.getRemaningTime();
		if (time >= 0 && time < 20) {
			return 20 - time;
		}
		return 0;
	}
}
