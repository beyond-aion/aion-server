package com.aionemu.gameserver.model.templates.event.upgradearcade;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcadeLevel {

	@XmlAttribute
	private int level;
	@XmlAttribute
	private String icon;
	@XmlAttribute(name = "upgrade_chance")
	private float upgradeChance;

	public int getLevel() {
		return level;
	}

	public String getIcon() {
		return icon;
	}

	public float getUpgradeChance() {
		return upgradeChance;
	}
}
