package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Runs its operations when the player enters one of the listed worlds, or any world if none are listed.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnEnterWorldEvent")
public class OnEnterWorldEvent extends QuestEvent {

	@XmlAttribute(name = "world_ids")
	protected List<Integer> worldIds;

	@Override
	public boolean operate(QuestEnv env) {
		if (worldIds != null && !worldIds.contains(env.getPlayer().getWorldId()))
			return false;
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
