package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author kecimis
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Motion")
public class Motion {

	@XmlAttribute(required = true)
	protected String name;// TODO enum

	@XmlAttribute
	protected int speed = 100;

	@XmlAttribute(name = "instant_skill")
	protected boolean instantSkill = false;

	public String getName() {
		return this.name;
	}

	public int getSpeed() {
		return this.speed;
	}

	public boolean getInstantSkill() {
		return this.instantSkill;
	}
}
