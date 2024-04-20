package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.KillInWorld;

/**
 * @author vlog, Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillInWorldData")
public class KillInWorldData extends XMLQuest {

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

	@XmlAttribute(name = "worlds")
	protected List<Integer> worldIds;

	@XmlAttribute(name = "invasion_world")
	protected int invasionWorld;

	@XmlAttribute(name = "start_dialog_id")
	protected int startDialogId;

	@XmlAttribute(name = "start_dist_npc_id")
	protected int startDistanceNpcId;

	@XmlAttribute(name = "end_dialog_id")
	protected int endDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new KillInWorld(id, endNpcIds, startNpcIds, worldIds, amount, minRank, levelDiff, invasionWorld, startDialogId,
			startDistanceNpcId, endDialogId));
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
