package com.aionemu.gameserver.model.templates.event.upgradearcade;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ArcadeRewards")
public class ArcadeRewards {

	@XmlAttribute(name = "min_level")
	private int minLevel;
	@XmlElement(name = "item")
	private List<ArcadeRewardItem> arcadeRewardItems;

	public int getMinLevel() {
		return minLevel;
	}

	public List<ArcadeRewardItem> getArcadeRewardItems() {
		return arcadeRewardItems;
	}
}
