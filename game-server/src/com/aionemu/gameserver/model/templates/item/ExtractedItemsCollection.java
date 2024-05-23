package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Chance;

/**
 * @author antness
 */
@XmlType(name = "ExtractedItemsCollection")
public class ExtractedItemsCollection extends ResultedItemsCollection implements Chance {

	@XmlAttribute(name = "chance")
	private float chance = 100f;
	@XmlAttribute(name = "minlevel")
	private int minLevel;
	@XmlAttribute(name = "maxlevel")
	private int maxLevel = 99;

	public final float getChance() {
		return chance;
	}

	public final int getMinLevel() {
		return minLevel;
	}

	public final int getMaxLevel() {
		return maxLevel;
	}

}
