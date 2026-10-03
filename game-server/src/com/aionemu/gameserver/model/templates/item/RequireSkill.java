package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireSkill")
public class RequireSkill {

	@XmlAttribute
	protected List<Integer> skillIds;

	public List<Integer> getSkillIds() {
		if (skillIds == null) {
			skillIds = new ArrayList<>();
		}
		return this.skillIds;
	}

}
