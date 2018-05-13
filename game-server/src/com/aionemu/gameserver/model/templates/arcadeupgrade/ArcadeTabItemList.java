package com.aionemu.gameserver.model.templates.arcadeupgrade;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ArcadeTabItemList")
public class ArcadeTabItemList {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "normal_count")
	private int normalCount;
	@XmlAttribute(name = "frenzy_count")
	private int frenzyCount;

	public final int getItemId() {
		return itemId;
	}

	public final int getNormalCount() {
		return normalCount;
	}

	public final int getFrenzyCount() {
		return frenzyCount;
	}
}
