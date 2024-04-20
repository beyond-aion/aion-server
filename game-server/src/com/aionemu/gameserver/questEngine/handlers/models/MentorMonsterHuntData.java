package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestKill;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.MentorMonsterHunt;

/**
 * @author MrPoke, Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MentorMonsterHuntData")
public class MentorMonsterHuntData extends MonsterHuntData {

	@XmlAttribute(name = "min_mente_level")
	protected int minMenteLevel = 1;

	@XmlAttribute(name = "max_mente_level")
	protected int maxMenteLevel = 99;

	public int getMinMenteLevel() {
		return minMenteLevel;
	}

	public int getMaxMenteLevel() {
		return maxMenteLevel;
	}

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

		questEngine.addQuestHandler(new MentorMonsterHunt(id, startNpcIds, endNpcIds, monsters, minMenteLevel, maxMenteLevel, reward, rewardNextStep));
	}
}
