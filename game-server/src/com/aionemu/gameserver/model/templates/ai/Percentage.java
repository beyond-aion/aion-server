package com.aionemu.gameserver.model.templates.ai;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Percentage")
public class Percentage {

	@XmlAttribute(name = "percent")
	protected int percent;
	@XmlAttribute(name = "skillId")
	protected int skillId = 0;
	@XmlAttribute(name = "isIndividual")
	protected boolean isIndividual = false;
	@XmlElement(name = "summonGroup")
	protected List<SummonGroup> summons;

	public List<SummonGroup> getSummons() {
		return summons;
	}

	public int getPercent() {
		return percent;
	}

	public int getSkillId() {
		return skillId;
	}

	public boolean isIndividual() {
		return isIndividual;
	}
}
