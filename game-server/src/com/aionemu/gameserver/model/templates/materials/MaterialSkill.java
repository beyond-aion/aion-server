package com.aionemu.gameserver.model.templates.materials;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaterialSkill")
public class MaterialSkill {

	@XmlAttribute
	protected MaterialActTime time;

	@XmlAttribute(required = true)
	protected float frequency;

	@XmlAttribute
	protected MaterialTarget target;

	@XmlAttribute(required = true)
	protected int level;

	@XmlAttribute(required = true)
	protected int id;

	public MaterialActTime getTime() {
		return time;
	}

	public float getFrequency() {
		return frequency;
	}

	public MaterialTarget getTarget() {
		if (target == null)
			return MaterialTarget.ALL;
		return target;
	}

	public int getSkillLevel() {
		return level;
	}

	public int getId() {
		return id;
	}

}
