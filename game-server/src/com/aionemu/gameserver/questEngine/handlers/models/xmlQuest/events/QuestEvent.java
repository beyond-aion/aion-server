package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions.QuestConditions;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations.QuestOperations;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestEvent", propOrder = { "conditions", "operations" })
@XmlSeeAlso({ OnKillEvent.class, OnTalkEvent.class })
public abstract class QuestEvent {

	protected QuestConditions conditions;
	protected QuestOperations operations;
	@XmlAttribute
	protected List<Integer> ids;

	public boolean operate(QuestEnv env) {
		return false;
	}

	public List<Integer> getIds() {
		return ids == null ? List.of() : ids;
	}
}
