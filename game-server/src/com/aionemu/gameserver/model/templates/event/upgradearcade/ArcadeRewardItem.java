package com.aionemu.gameserver.model.templates.event.upgradearcade;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ArcadeRewardItem")
public class ArcadeRewardItem {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "normal_count")
	private long normalCount;
	@XmlAttribute(name = "frenzy_count")
	private long frenzyCount;

	public int getItemId() {
		return itemId;
	}

	public long getNormalCount() {
		return normalCount;
	}

	public long getFrenzyCount() {
		return frenzyCount;
	}
}
