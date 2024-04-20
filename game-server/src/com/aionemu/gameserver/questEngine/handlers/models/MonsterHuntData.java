package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestKill;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.MonsterHunt;

/**
 * @author MrPoke, Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonsterHuntData")
@XmlSeeAlso({ KillSpawnedData.class, MentorMonsterHuntData.class })
public class MonsterHuntData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	protected int startDialogId;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialogId;

	@XmlAttribute(name = "aggro_start_npc_ids")
	protected List<Integer> aggroNpcIds;

	@XmlAttribute(name = "invasion_world")
	protected int invasionWorld;

	@XmlAttribute(name = "start_zone")
	protected String startZone;

	@XmlAttribute(name = "start_dist_npc_id")
	protected int startDistanceNpcId;

	@XmlAttribute(name = "end_reward")
	protected boolean reward;

	@XmlAttribute(name = "end_reward_next_step")
	protected boolean rewardNextStep;

	@Override
	public void register(QuestEngine questEngine) {
		List<Monster> monsters;
		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(id);

		if (!questTemplate.getQuestKill().isEmpty()) {
			monsters = new ArrayList<>();
			for (QuestKill qk : questTemplate.getQuestKill()) {
				Monster m = new Monster();
				if (qk.getKillCount() > 0)
					m.setEndVar(qk.getKillCount());
				if (qk.getNpcIds() != null)
					m.addNpcIds(qk.getNpcIds());
				if (qk.getVar() > 0)
					m.setVar(qk.getVar());
				if (qk.getQuestStep() > 0)
					m.setStep(qk.getQuestStep());
				if (qk.getSequenceNumber() > 0)
					m.setVar(qk.getSequenceNumber());
				monsters.add(m);
			}
		} else {
			monsters = Collections.emptyList();
		}

		questEngine.addQuestHandler(new MonsterHunt(id, startNpcIds, endNpcIds, monsters, startDialogId, endDialogId, aggroNpcIds, invasionWorld,
			startZone, startDistanceNpcId, reward, rewardNextStep));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (endNpcIds != null && endNpcIds.size() > 1 && endNpcIds.contains(npcId))
			return endNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (aggroNpcIds != null && aggroNpcIds.size() > 1 && aggroNpcIds.contains(npcId))
			return aggroNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		for (QuestKill qk : DataManager.QUEST_DATA.getQuestById(id).getQuestKill()) {
			List<Integer> npcIds = qk.getNpcIds();
			if (npcIds != null && npcIds.size() > 1 && npcIds.contains(npcId))
				return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		}
		return null;
	}
}
