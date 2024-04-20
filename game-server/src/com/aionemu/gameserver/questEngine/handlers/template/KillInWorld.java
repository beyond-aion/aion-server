package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.VortexService;

/**
 * Standard xml-based handling for the DAILY quests with onKillInZone events
 * 
 * @author vlog, bobobear, Pad
 */
public class KillInWorld extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(KillInWorld.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final Set<Integer> worldIds = new HashSet<>();
	private final int killAmount;
	private final int minRank;
	private final int levelDiff;
	private final int invasionWorldId;
	private final int startDialogId;
	private final int startDistanceNpcId;
	private final int endDialogId;
	private final boolean isDataDriven;

	public KillInWorld(int questId, List<Integer> endNpcIds, List<Integer> startNpcIds, List<Integer> worldIds, int killAmount, int minRank,
		int levelDiff, int invasionWorld, int startDialogId, int startDistanceNpcId, int endDialogId) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		if (worldIds != null) {
			this.worldIds.addAll(worldIds);
		} else {
			for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA)
				this.worldIds.add(template.getMapId());
		}
		if (killAmount == 0)
			this.killAmount = 1;
		else
			this.killAmount = killAmount;
		this.minRank = minRank;
		this.levelDiff = levelDiff;
		this.invasionWorldId = invasionWorld;
		this.startDialogId = startDialogId;
		this.startDistanceNpcId = startDistanceNpcId;
		this.endDialogId = endDialogId;
		this.isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
		if (workItems != null)
			log.warn("Q{} should not have work items", questId);
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds)
				qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		}
		for (Integer worldId : worldIds)
			qe.registerOnKillInWorld(worldId, questId);

		if (invasionWorldId != 0)
			qe.registerOnEnterWorld(questId);

		if (startDistanceNpcId != 0)
			qe.registerQuestNpc(startDistanceNpcId, 300).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : isDataDriven ? 4762 : 1011);
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
					default:
						return super.onDialogEvent(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId)) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, endDialogId != 0 ? endDialogId : isDataDriven ? 10002 : 2375);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		VortexLocation vortexLoc = VortexService.getInstance().getLocationByWorld(invasionWorldId);
		if (player.getWorldId() == invasionWorldId) {
			if (qs == null || qs.isStartable()) {
				if (vortexLoc != null && vortexLoc.isActive() || searchOpenRift())
					return QuestService.startQuest(env);
			}
		}
		return false;
	}

	private boolean searchOpenRift() {
		for (RiftLocation loc : RiftService.getInstance().getRiftLocations().values()) {
			if (loc.getWorldId() == invasionWorldId && loc.isOpened()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillInWorldEvent(QuestEnv env) {
		// Rank restriction
		if (minRank > 0 && ((Player) env.getVisibleObject()).getAbyssRank().getRank().getId() < minRank)
			return false;
		// Level restriction
		if (levelDiff > 0 && (env.getPlayer().getLevel() - ((Player) env.getVisibleObject()).getLevel()) > levelDiff)
			return false;
		return defaultOnKillRankedEvent(env, 0, killAmount, true, isDataDriven); // reward
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable())
			return QuestService.startQuest(env);
		return false;
	}
}
