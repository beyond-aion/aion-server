package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.KillInWorld;

/**
 * @author vlog
 * @reworked Bobobear
 * @modified Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillInWorldData")
public class KillInWorldData extends XMLQuest {

	@XmlAttribute(name = "start_npc_ids")
	protected List<Integer> startNpcIds;
	@XmlAttribute(name = "end_npc_ids", required = true)
	protected List<Integer> endNpcIds;
	@XmlAttribute(name = "amount")
	protected int amount;
	@XmlAttribute(name = "min_rank")
	protected int minRank;
	@XmlAttribute(name = "level_diff")
	protected int levelDiff;
	@XmlAttribute(name = "worlds", required = true)
	protected List<Integer> worldIds;
	@XmlAttribute(name = "invasion_world")
	protected int invasionWorld;
	@XmlAttribute(name = "start_dialog_id")
	protected int startDialogId;
	@XmlAttribute(name = "start_dist_npc_id")
	protected int startDistanceNpc;
	@XmlAttribute(name = "end_dialog_id")
	protected int endDialogId;

	@Override
	public void register(QuestEngine questEngine) {
		if (worldIds.size() == 1 && worldIds.contains(0)) {
			Iterator<WorldMapTemplate> itr = DataManager.WORLD_MAPS_DATA.iterator();
			worldIds.clear();
			while (itr.hasNext()) {
				WorldMapTemplate template = itr.next();
				worldIds.add(template.getMapId());
			}
		}
		KillInWorld template = new KillInWorld(id, endNpcIds, startNpcIds, worldIds, amount, minRank, levelDiff, invasionWorld, startDialogId,
			startDistanceNpc, endDialogId);
		questEngine.addQuestHandler(template);
	}
}
