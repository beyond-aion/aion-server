package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Runs its operations when the players level changed, optionally only from the given level upwards.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnLevelUpEvent")
public class OnLevelUpEvent extends QuestEvent {

	@XmlAttribute
	protected int level;

	@Override
	public boolean operate(QuestEnv env) {
		if (level > 0 && env.getPlayer().getLevel() < level)
			return false;
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
