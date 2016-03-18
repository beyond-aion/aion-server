package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 * @modified Majka, Pad
 */
public class KillInZone extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(KillInZone.class);

	private final Set<Integer> startNpcs = new HashSet<>();
	private final Set<Integer> endNpcs = new HashSet<>();
	private final int killAmount;
	private final int minRank;
	private final int levelDiff;
	private final int startDistanceNpc;
	private final Set<String> zones = new HashSet<>();
	private final boolean isDataDriven;

	public KillInZone(int questId, List<Integer> endNpcIds, List<Integer> startNpcIds, List<String> zones, int killAmount, int minRank, int levelDiff,
		int startDistanceNpc) {
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
		this.zones.addAll(zones);
		this.killAmount = killAmount;
		this.minRank = minRank;
		this.levelDiff = levelDiff;
		this.startDistanceNpc = startDistanceNpc;
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		if (workItems.size() > 0)
			log.warn("Q{} (KillInWorld) has a work item.", questId);
	}

	@Override
	public void register() {
		for (Integer startNpc : startNpcs) {
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}
		for (Integer endNpc : endNpcs)
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
		for (String zone : zones)
			qe.registerOnKillInZone(zone, questId);
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
						return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
					}
					case QUEST_ACCEPT_1: {
						return sendQuestStartDialog(env);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcs.contains(targetId)) {
				if (isDataDriven && dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillInZoneEvent(QuestEnv env) {
		// Rank restriction
		if (minRank > 0 && ((Player) env.getVisibleObject()).getAbyssRank().getRank().getId() < minRank)
			return false;
		// Level restriction
		if (levelDiff > 0 && (env.getPlayer().getLevel() - ((Player) env.getVisibleObject()).getLevel()) > levelDiff)
			return false;
		return defaultOnKillInZoneEvent(env, 0, killAmount, true, isDataDriven); // reward
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
