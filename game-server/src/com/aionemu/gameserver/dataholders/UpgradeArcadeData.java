package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeLevel;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeLevels;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeRewards;

/**
 * @author ginho1
 */
@XmlRootElement(name = "arcadelist")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpgradeArcadeData {

	@XmlElement(name = "levels")
	private ArcadeLevels levels;
	@XmlElement(name = "rewards")
	private List<ArcadeRewards> rewards;

	public int size() {
		return rewards.size();
	}

	public int getMinResumableLevel() {
		return levels.getMinResumableLevel();
	}

	public List<ArcadeLevel> getUpgradeLevels() {
		return levels.getLevels();
	}

	public ArcadeLevel getMaxUpgradeLevel() {
		return levels.getMaxUpgradeLevel();
	}

	public List<ArcadeRewards> getRewards() {
		return rewards;
	}
}
