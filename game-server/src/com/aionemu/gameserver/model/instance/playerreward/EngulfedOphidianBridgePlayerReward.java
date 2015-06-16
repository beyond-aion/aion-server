package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;

/**
 *
 * @author Tibald
 */
public class EngulfedOphidianBridgePlayerReward extends InstancePlayerReward {

	private Race race;
	private int ophidianBox, oBOpportunityBundle, baseReward, bonusReward, gloryPoints;
	
	public EngulfedOphidianBridgePlayerReward(Integer object, Race race) {
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

	public void setOphidianBox(int ophidianBox) {
		this.ophidianBox = ophidianBox;
	}

	public int getOphidianBox() {
		return ophidianBox;
	}
	
	public void setOBOpportunityBundle(int oBOpportunityBundle) {
		this.oBOpportunityBundle = oBOpportunityBundle;
	}

	public int getOBOpportunityBundle() {
		return oBOpportunityBundle;
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
