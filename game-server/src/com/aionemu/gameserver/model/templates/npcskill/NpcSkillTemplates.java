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
@XmlType(name = "npc_skills")
public class NpcSkillTemplates {

	@XmlList
	@XmlAttribute(name = "npc_ids")
	protected List<Integer> npcIds;
	@XmlElement(name = "npc_skill")
	protected List<NpcSkillTemplate> npcSkills;

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public List<NpcSkillTemplate> getNpcSkills() {
		return npcSkills;
	}

}
