package com.aionemu.gameserver.model.templates.expand;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Simple
 */
@XmlRootElement(name = "expand")
@XmlAccessorType(XmlAccessType.FIELD)
public class Expand {

	@XmlAttribute(name = "level", required = true)
	protected int level;
	@XmlAttribute(name = "price", required = true)
	protected int price;

	public int getLevel() {
		return level;
	}

	public int getPrice() {
		return price;
	}
}
