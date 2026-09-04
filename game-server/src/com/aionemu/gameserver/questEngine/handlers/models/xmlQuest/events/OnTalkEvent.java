package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.QuestVar;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnTalkEvent", propOrder = { "var" })
public class OnTalkEvent extends QuestEvent {

	@XmlElement(name = "var")
	protected List<QuestVar> var;

	/** @return True if the quest expects the player to talk to the given npc in its current state. */
	public boolean isWaitingFor(QuestEnv env, int npcId) {
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
		for (QuestVar questVar : var) {
			if (questVar.isCurrentAndHandles(qs, npcId))
				return true;
		}
		return false;
	}

	@Override
	public boolean operate(QuestEnv env) {
		if (conditions == null || conditions.checkConditionOfSet(env)) {
			QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
			for (QuestVar questVar : var) {
				if (questVar.operate(env, qs))
					return true;
			}
		}
		return false;
	}
}
