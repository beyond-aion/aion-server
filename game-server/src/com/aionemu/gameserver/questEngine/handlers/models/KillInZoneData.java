package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.KillInZone;

/**
 * @author Cheatkiller, Majka, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillInZoneData")
public class KillInZoneData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;
	
	@XmlAttribute(name = "end_npc_ids")
	protected List<Integer> endNpcIds;
	
	@XmlAttribute(name = "amount")
	protected int amount;
	
	@XmlAttribute(name = "min_rank")
	protected int minRank;
	
	@XmlAttribute(name = "level_diff")
	protected int levelDiff;
	
	@XmlAttribute(name = "zones")
	protected List<String> zones;
	
	@XmlAttribute(name = "start_dist_npc_id")
	protected int startDistanceNpc;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new KillInZone(id, endNpcIds, startNpcIds, zones, amount, minRank, levelDiff, startDistanceNpc));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		if (startNpcIds != null && startNpcIds.size() > 1 && startNpcIds.contains(npcId))
			return startNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		if (endNpcIds != null && endNpcIds.size() > 1 && endNpcIds.contains(npcId))
			return endNpcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		return null;
	}
}
