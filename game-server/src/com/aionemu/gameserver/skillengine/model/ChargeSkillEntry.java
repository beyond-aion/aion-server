package com.aionemu.gameserver.skillengine.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeSkill", propOrder = { "skills" })
public class ChargeSkillEntry {

	@XmlElement(name = "skill", required = true)
	protected List<ChargedSkill> skills;

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "min_time", required = true)
	protected int minTime;

	public List<ChargedSkill> getSkills() {
		return skills;
	}

	/**
	 * Gets the value of the minTime property.
	 */
	public int getMinTime() {
		return minTime;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}
}
