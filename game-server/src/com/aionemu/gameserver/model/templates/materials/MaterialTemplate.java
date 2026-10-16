package com.aionemu.gameserver.model.templates.materials;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.Creature;

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

	public List<MaterialSkill> getSkills(Creature creature) {
		return skills.stream().filter(skill -> skill.getTarget().matches(creature)).toList();
	}

	public Integer getSkillObstacle() {
		return skillObstacle;
	}

	public int getId() {
		return id;
	}

}
