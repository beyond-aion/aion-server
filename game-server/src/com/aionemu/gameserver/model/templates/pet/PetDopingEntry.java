package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "dope")
@XmlAccessorType(XmlAccessType.NONE)
public class PetDopingEntry {

	@XmlAttribute(name = "id", required = true)
	private short id;

	@XmlAttribute(name = "usedrink", required = true)
	private boolean usedrink;

	@XmlAttribute(name = "usefood", required = true)
	private boolean usefood;

	@XmlAttribute(name = "usescroll", required = true)
	private int usescroll;

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	/**
	 * @return the usedrink
	 */
	public boolean isUseDrink() {
		return usedrink;
	}

	/**
	 * @return the usefood
	 */
	public boolean isUseFood() {
		return usefood;
	}

	/**
	 * @return the usescroll
	 */
	public int getScrollsUsed() {
		return usescroll;
	}

}
