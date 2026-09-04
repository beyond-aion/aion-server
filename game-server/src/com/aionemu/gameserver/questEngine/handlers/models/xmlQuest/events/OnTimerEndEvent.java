package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Runs its operations when a timer started by {@code add_timer} expires. Timers don't survive a relog, so the operations must leave the quest
 * in a state the player can continue from.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnTimerEndEvent")
public class OnTimerEndEvent extends QuestEvent {

	public boolean operate(QuestEnv env) {
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
