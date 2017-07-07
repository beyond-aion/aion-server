package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.Unmarshaller;
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
	private String name;// TODO enum

	@XmlAttribute
	private int speed = 100;

	@XmlAttribute(name = "instant_skill")
	private boolean instantSkill = false;

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		if (name != null)
			name = name.intern(); // intern to save RAM
	}

	public String getName() {
		return name;
	}

	/**
	 * @return Animation play speed in percent
	 */
	public int getSpeed() {
		return speed;
	}

	public boolean isInstantSkill() {
		return instantSkill;
	}
}
