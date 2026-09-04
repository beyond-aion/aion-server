package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestVar", propOrder = { "npc" })
public class QuestVar {

	@XmlElement(name = "npc")
	protected List<QuestNpc> npc;
	
	@XmlAttribute(required = true)
	protected int value;

	/** @return True if this branch is the current one and holds a dialog for the given npc. */
	public boolean isCurrentAndHandles(QuestState qs, int npcId) {
		if ((qs == null ? -1 : qs.getQuestVars().getQuestVars()) != value)
			return false;
		for (QuestNpc questNpc : npc) {
			if (questNpc.getId() == npcId)
				return true;
		}
		return false;
	}

	public boolean operate(QuestEnv env, QuestState qs) {
		int var = -1;
		if (qs != null)
			var = qs.getQuestVars().getQuestVars();
		if (var != value)
			return false;
		for (QuestNpc questNpc : npc) {
			if (questNpc.operate(env, qs))
				return true;
		}
		return false;
	}
}
