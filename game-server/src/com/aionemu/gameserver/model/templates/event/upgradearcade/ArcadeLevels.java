package com.aionemu.gameserver.model.templates.event.upgradearcade;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcadeLevels {

	@XmlAttribute(name = "min_resumable_level")
	private int minResumableLevel;
	@XmlElement(name = "level")
	private List<ArcadeLevel> upgradeLevels;

	public int getMinResumableLevel() {
		return minResumableLevel;
	}

	public List<ArcadeLevel> getLevels() {
		return upgradeLevels;
	}

	public ArcadeLevel getMaxUpgradeLevel() {
		return upgradeLevels.get(upgradeLevels.size() - 1);
	}
}
