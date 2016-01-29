package com.aionemu.gameserver.model.templates.npc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Artur
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MassiveLoot")
public class MassiveLoot {

	@XmlAttribute(name = "m_loot_count")
	private int massiveLootCount;

	@XmlAttribute(name = "m_loot_item")
	private int massiveLootItem;

	@XmlAttribute(name = "m_loot_min_level")
	private int massiveLootMinLevel;

	@XmlAttribute(name = "m_loot_max_level")
	private int massiveLootMaxLevel;

	/**
	 * @return the massiveLootCount
	 */
	public int getMLootCount() {
		return massiveLootCount;
	}

	/**
	 * @return the massiveLootItem
	 */
	public int getMLootItem() {
		return massiveLootItem;
	}

	/**
	 * @return the massiveLootMinLevel
	 */
	public int getMLootMinLevel() {
		return massiveLootMinLevel;
	}

	/**
	 * @return the massiveLootMaxLevel
	 */
	public int getMLootMaxLevel() {
		return massiveLootMaxLevel;
	}
}
