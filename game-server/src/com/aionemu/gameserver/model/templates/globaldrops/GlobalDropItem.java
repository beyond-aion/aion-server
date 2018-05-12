package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropItem")
public class GlobalDropItem {

	@XmlAttribute(name = "id", required = true)
	private int itemId;
	@XmlAttribute(name = "chance")
	private float chance = 100f;

	public int getId() {
		return itemId;
	}

	public float getChance() {
		return chance;
	}
}
