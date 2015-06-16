package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreatureSpeeds")
public class CreatureSpeeds {

	@XmlAttribute(name = "walk")
	private float walkSpeed;

	@XmlAttribute(name = "run")
	private float runSpeed;

	@XmlAttribute(name = "group_walk")
	private float groupWalkSpeed;

	@XmlAttribute(name = "run_fight")
	private float runSpeedFight;

	@XmlAttribute(name = "group_run_fight")
	private float groupRunSpeedFight;

	@XmlAttribute(name = "fly")
	private float flySpeed;

	public float getWalkSpeed() {
		return walkSpeed;
	}

	public float getRunSpeed() {
		return runSpeed;
	}

	public float getFlySpeed() {
		return flySpeed;
	}

	public float getGroupWalkSpeed() {
		return groupWalkSpeed;
	}

	public float getRunSpeedFight() {
		return runSpeedFight;
	}

	public float getGroupRunSpeedFight() {
		return groupRunSpeedFight;
	}
}
