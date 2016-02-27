package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestKill;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.MonsterHunt;

/**
 * @author MrPoke
 * @modified Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonsterHuntData", propOrder = { "monster" })
@XmlSeeAlso({ KillSpawnedData.class, MentorMonsterHuntData.class })
public class MonsterHuntData extends XMLQuest {

	@XmlElement(name = "monster")
	protected List<Monster> monster;

	@XmlAttribute(name = "start_npc_ids", required = true)
	protected List<Integer> startNpcIds;

	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;

	@XmlAttribute(name = "start_dialog_id")
	protected int startDialog;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialog;

	@XmlAttribute(name = "aggro_start_npcs")
	protected List<Integer> aggroNpcs;

	@XmlAttribute(name = "invasion_world")
	protected int invasionWorld;

	@XmlAttribute(name = "start_zone")
	protected String startZone;

	@XmlAttribute(name = "start_dist_npc_id")
	protected int startDistanceNpc;

	@XmlAttribute(name = "end_reward_next_step")
	protected boolean rewardNextStep;

	@Override
	public void register(QuestEngine questEngine) {
		Map<Monster, Set<Integer>> monsterNpcs = new FastMap<>();
		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(id);

		if (questTemplate.getQuestKill() != null && questTemplate.getQuestKill().size() > 0) {
			for (QuestKill qk : questTemplate.getQuestKill()) {
				Monster mn = new Monster();
				if (qk.getKillCount() > 0)
					mn.setEndVar(qk.getKillCount());
				if (qk.getNpcIds() != null)
					mn.addNpcIds(qk.getNpcIds());
				if (qk.getVar() >= 0)
					mn.setVar(qk.getVar());
				if (qk.getQuestStep() >= 0)
					mn.setStep(qk.getQuestStep()); // Quest step
				if (qk.getSequenceNumber() >= 0)
					mn.setVar(qk.getSequenceNumber());
				// if monster != null then try to add into mn all values
				if (monster != null) {
					for (Monster m : monster) {
						// if monster with the same var and step is present, the values from quest template will be overrided (excluding npcs who will be merged)
						if (m.getVar() == mn.getVar() && m.getStep().equals(mn.getStep())) {
							if (m.getStartVar() != null)
								mn.setStartVar(m.getStartVar());
							if (m.getEndVar() > 0)
								mn.setEndVar(m.getEndVar());
							if (m.getRewardVar())
								mn.setRewardVar(m.getRewardVar());
							if (m.getNpcIds() != null)
								mn.addNpcIds(m.getNpcIds());
							if (m.getNpcSequence() != null)
								mn.setNpcSequence(m.getNpcSequence());
							if (m.getSpawnerObject() > 0)
								mn.setSpawnerObject(m.getSpawnerObject());
						}
					}
				}
				monsterNpcs.put(mn, new HashSet<>(mn.getNpcIds()));
			}
		} else if (monster != null) {
			for (Monster m : monster) {
				monsterNpcs.put(m, new HashSet<>(m.getNpcIds()));
			}
		}

		/**
		 * for (Monster m : monster) { if (CustomConfig.QUESTDATA_MONSTER_KILLS) { // if sequence numbers specified use it if (m.getNpcSequence() != null
		 * && questTemplate.getQuestKill() != null) { QuestKill killNpcs = null; for (int index = 0; index < questTemplate.getQuestKill().size(); index++)
		 * { if (questTemplate.getQuestKill().get(index).getSequenceNumber() == m.getNpcSequence()) { killNpcs = questTemplate.getQuestKill().get(index);
		 * break; } } if (killNpcs != null) monsterNpcs.put(m, killNpcs.getNpcIds()); } // if no sequence was specified, check all npc ids to match quest
		 * data else if (m.getNpcSequence() == null && questTemplate.getQuestKill() != null) { Set<Integer> npcSet = new HashSet<Integer>(m.getNpcIds());
		 * QuestKill matchedKillNpcs = null; int maxMatchCount = 0; for (int index = 0; index < questTemplate.getQuestKill().size(); index++) { QuestKill
		 * killNpcs = questTemplate.getQuestKill().get(index); int matchCount = 0; for (int npcId : killNpcs.getNpcIds()) { if (!npcSet.contains(npcId))
		 * continue; matchCount++; } if (matchCount > maxMatchCount) { maxMatchCount = matchCount; matchedKillNpcs = killNpcs; } } if (matchedKillNpcs !=
		 * null) { // add npcs not present in quest data (weird!) npcSet.addAll(matchedKillNpcs.getNpcIds()); monsterNpcs.put(m, npcSet); } } else {
		 * monsterNpcs.put(m, new HashSet<Integer>(m.getNpcIds())); } } else { monsterNpcs.put(m, new HashSet<Integer>(m.getNpcIds())); } }
		 **/

		MonsterHunt template = new MonsterHunt(id, startNpcIds, endNpcIds, monsterNpcs, startDialog, endDialog, aggroNpcs, invasionWorld, startZone,
			startDistanceNpc, rewardNextStep);
		questEngine.addQuestHandler(template);
	}

}
