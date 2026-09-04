package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Runs its operations when the player enters one of the listed zones.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnEnterZoneEvent")
public class OnEnterZoneEvent extends QuestEvent {

	@XmlAttribute(name = "zones", required = true)
	protected List<String> zones;

	public List<String> getZones() {
		if (zones == null)
			zones = new ArrayList<>();
		return zones;
	}

	public boolean operate(QuestEnv env, ZoneName zoneName) {
		if (zones == null || !zones.contains(zoneName.name()))
			return false;
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
