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

	@XmlAttribute(name = "startlevel", required = false)
	private int startLevel;

	@XmlAttribute(name = "interval", required = true)
	private int interval;

	/**
	 * @return the dropItem
	 */
	public int getDropItem() {
		return dropItem;
	}

	/**
	 * @return the startLevel
	 */
	public int getStartLevel() {
		return startLevel;
	}

	/**
	 * @return the interval in minutes
	 */
	public int getInterval() {
		return interval;
	}
}
