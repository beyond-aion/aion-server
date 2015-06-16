package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
