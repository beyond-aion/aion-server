package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Improvement")
public class Improvement {

	@XmlAttribute(name = "way", required = true)
	private int way;

	@XmlAttribute(name = "price2" )
	private int price2;

	@XmlAttribute(name = "price1")
	private int price1;

	@XmlAttribute(name = "burn_defend")
	private int burnDefend;

	@XmlAttribute(name = "burn_attack")
	private int burnAttack;

	@XmlAttribute(name = "level")
	private int level;

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the way
	 */
	public int getChargeWay() {
		return way;
	}

	/**
	 * @return the price1
	 */
	public int getPrice1() {
		return price1;
	}

	/**
	 * @return the price2
	 */
	public int getPrice2() {
		return price2;
	}

	public int getBurnAttack() {
		return burnAttack;
	}

	public int getBurnDefend() {
		return burnDefend;
	}

}
