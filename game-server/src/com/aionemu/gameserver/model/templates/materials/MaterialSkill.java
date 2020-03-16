package com.aionemu.gameserver.model.templates.materials;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaterialSkill")
public class MaterialSkill {

	@XmlAttribute
	@XmlList
	private List<MaterialActCondition> conditions;

	@XmlAttribute(required = true)
	private int frequency;

	@XmlAttribute
	private MaterialTarget target;

	@XmlAttribute(required = true)
	private int level;

	@XmlAttribute(required = true)
	private int id;

	public List<MaterialActCondition> getConditions() {
		return conditions == null ? Collections.emptyList() : conditions;
	}

	public int getFrequency() {
		return frequency;
	}

	public MaterialTarget getTarget() {
		return target == null ? MaterialTarget.ALL : target;
	}

	public int getSkillLevel() {
		return level;
	}

	public int getId() {
		return id;
	}

}
