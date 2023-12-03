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

	/**
	 * name is null when instant_skill is true<br/>
	 * Note: There are also several skill IDs of Ambush with no motion name and speed 85, which is just ignorable junk data parsed from the game client
	 */
	@XmlAttribute
	private String name;

	@XmlAttribute
	private int speed = 100;

	@XmlAttribute(name = "instant_skill")
	private boolean instantSkill = false;

	@XmlAttribute(name = "delay")
	private int delay = 0;

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

	public int getDelay() {
		return delay;
	}
}
