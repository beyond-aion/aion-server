package com.aionemu.gameserver.model.templates.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Rolandas
 */
@XmlType(name = "InventoryDrop")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventoryDrop {

	@XmlValue
	private int dropItem;

	@XmlAttribute(name = "startlevel")
	private int startLevel;
	@XmlAttribute(name = "interval", required = true)
	private int interval;
	@XmlAttribute(name = "count")
	private int count = 1;

	public int getDropItem() {
		return dropItem;
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
