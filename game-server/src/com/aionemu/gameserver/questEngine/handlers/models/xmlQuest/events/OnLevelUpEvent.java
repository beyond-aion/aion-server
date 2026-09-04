package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
