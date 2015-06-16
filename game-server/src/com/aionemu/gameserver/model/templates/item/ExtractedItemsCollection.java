package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "ExtractedItemsCollection")
public class ExtractedItemsCollection extends ResultedItemsCollection {

	@XmlAttribute(name = "chance")
	protected float chance = 100f;
	@XmlAttribute(name = "minlevel")
	protected int minLevel;
	@XmlAttribute(name = "maxlevel")
	protected int maxLevel;

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
