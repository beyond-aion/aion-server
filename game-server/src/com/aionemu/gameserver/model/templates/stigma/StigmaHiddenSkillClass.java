package com.aionemu.gameserver.model.templates.stigma;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.PlayerClass;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StigmaHiddenSkillClass")
public class StigmaHiddenSkillClass {

	@XmlAttribute(required = true)
	private PlayerClass name;
	@XmlElement(name = "hidden_skill", required = true)
	private List<StigmaHiddenSkill> hiddenSkills;

	public PlayerClass getPlayerClass() {
		return name;
	}

	public List<StigmaHiddenSkill> getHiddenSkills() {
		return hiddenSkills;
	}
}
