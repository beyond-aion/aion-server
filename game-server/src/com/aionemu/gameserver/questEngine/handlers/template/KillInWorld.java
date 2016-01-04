package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.VortexService;

/**
 * Standard xml-based handling for the DAILY quests with onKillInZone events
 * 
 * @author vlog, reworked bobobear
 */
public class KillInWorld extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(KillInWorld.class);

	private final Set<Integer> startNpcs = new HashSet<>();
	private final Set<Integer> endNpcs = new HashSet<>();
	private final Set<Integer> worldIds = new HashSet<>();
	private final int killAmount;
	private final int minRank;
	private final int levelDiff;
	private final int invasionWorldId;
	private final int startDialog;
	private final int startDistanceNpc;
	private final boolean isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();

	public KillInWorld(int questId, List<Integer> endNpcIds, List<Integer> startNpcIds, List<Integer> worldIds, int killAmount, int minRank,
		int levelDiff, int invasionWorld, int startDialog, int startDistanceNpc) {
		super(questId);
		if (startNpcIds != null) {
			this.startNpcs.addAll(startNpcIds);
			this.startNpcs.remove(0);
		}
		if (endNpcIds == null) {
			this.endNpcs.addAll(startNpcs);
		} else {
			this.endNpcs.addAll(endNpcIds);
			this.endNpcs.remove(0);
		}
		this.worldIds.addAll(worldIds);
		this.worldIds.remove(0);
		this.killAmount = killAmount;
		this.minRank = minRank;
		this.levelDiff = levelDiff;
		this.invasionWorldId = invasionWorld;
		this.startDialog = startDialog;
		this.startDistanceNpc = startDistanceNpc;
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		if (workItems.size() > 0) {
			log.warn("Q{} (KillInWorld) has a work item.", questId);
		}
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = startNpcs.iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}
		iterator = endNpcs.iterator();
		while (iterator.hasNext()) {
			int endNpc = iterator.next();
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
		}
		iterator = worldIds.iterator();
		while (iterator.hasNext()) {
			int worldId = iterator.next();
			qe.registerOnKillInWorld(worldId, questId);
		}

		if (invasionWorldId != 0)
			qe.registerOnEnterWorld(questId);

		if (startDistanceNpc != 0)
			qe.registerQuestNpc(startDistanceNpc, 300).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcs.isEmpty() || startNpcs.contains(targetId)) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, startDialog != 0 ? startDialog : 4762);
					}
					case QUEST_ACCEPT_1: {
						return sendQuestStartDialog(env);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcs.contains(targetId)) {
				if (isDataDriven && dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
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
			if ((qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat())) {
				if ((vortexLoc != null && vortexLoc.isActive()) || (searchOpenRift()))
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
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			QuestService.startQuest(env);
			return true;
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (startNpcs != null)
				constantSpawns.addAll(startNpcs);
			if (endNpcs != null)
				constantSpawns.addAll(endNpcs);
		}
		return constantSpawns;
	}
}
