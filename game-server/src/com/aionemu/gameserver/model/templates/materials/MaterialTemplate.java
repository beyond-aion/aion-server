package com.aionemu.gameserver.model.templates.materials;

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
@XmlType(name = "MaterialTemplate", propOrder = { "skills" })
public class MaterialTemplate {

	@XmlElement(name = "skill", required = true)
	private List<MaterialSkill> skills;

	@XmlAttribute(name = "skill_obstacle")
	private Integer skillObstacle;

	@XmlAttribute(required = true)
	private int id;

	public List<MaterialSkill> getSkills() {
		return skills;
	}

	public Integer getSkillObstacle() {
		return skillObstacle;
	}

	public int getId() {
		return id;
	}

}
