package com.aionemu.gameserver.model.templates.event;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "InventoryDrop")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventoryDrop {

	@XmlAttribute(name = "item_id", required = true)
	private int itemId;
	@XmlAttribute(name = "startlevel")
	private int startLevel;
	@XmlAttribute(name = "interval", required = true)
	private int interval;
	@XmlAttribute(name = "count")
	private int count = 1;

	public int getItemId() {
		return itemId;
	}

	public int getStartLevel() {
		return startLevel;
	}

	public int getInterval() {
		return interval;
	}

	public int getCount() {
		return count;
	}
}
