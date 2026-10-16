package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargedSkill")
public class ChargedSkill {

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(required = true)
	protected int time;

	/**
	 * Gets the value of the time property.
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

}
