package com.aionemu.gameserver.model.templates.rewards;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Luzien, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullRewardItem")
public class FullRewardItem extends IdLevelReward {

	@XmlAttribute(name = "count")
	private long count;

	@XmlAttribute(name = "chance")
	private float chance;

	@Override
	public long getCount() {
		return count;
	}

	@Override
	public float getChance() {
		return chance;
	}
}
