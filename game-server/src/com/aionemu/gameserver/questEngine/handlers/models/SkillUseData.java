package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastMap;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.SkillUse;

/**
 * @author vlog
 * @modified Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillUseData")
public class SkillUseData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;
	
	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;
	
	@XmlElement(name = "skill", required = true)
	protected List<QuestSkillData> skills;

	@Override
	public void register(QuestEngine questEngine) {
		FastMap<List<Integer>, QuestSkillData> questSkills = new FastMap<List<Integer>, QuestSkillData>();
		for (QuestSkillData qsd : skills) {
			questSkills.put(qsd.getSkillIds(), qsd);
		}
		SkillUse questTemplate = new SkillUse(id, startNpcIds, endNpcIds, questSkills);
		questEngine.addQuestHandler(questTemplate);
	}
}
