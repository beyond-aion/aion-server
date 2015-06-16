package com.aionemu.gameserver.model.templates.expand;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Simple
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Expand")
public class Expand {

	@XmlAttribute(name = "level", required = true)
	protected int level;
	@XmlAttribute(name = "price", required = true)
	protected int price;

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}
}
