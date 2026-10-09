package com.aionemu.gameserver.model.templates.materials;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

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
