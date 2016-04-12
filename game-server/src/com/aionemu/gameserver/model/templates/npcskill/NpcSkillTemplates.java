package com.aionemu.gameserver.model.templates.npcskill;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionChs Master
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "npcskills")
public class NpcSkillTemplates {

	@XmlList
	@XmlAttribute(name = "npcid")
	protected List<Integer> npcIds;
	@XmlElement(name = "npcskill")
	protected List<NpcSkillTemplate> npcSkills;

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public List<NpcSkillTemplate> getNpcSkills() {
		return npcSkills;
	}

}
