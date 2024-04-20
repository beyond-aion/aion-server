package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller, Majka, Pad
 */
public class KillInZone extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(KillInZone.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final Set<String> zones = new HashSet<>();
	private final int killAmount;
	private final int minRank;
	private final int levelDiff;
	private final int startDistanceNpc;
	private final boolean isDataDriven;

	public KillInZone(int questId, List<Integer> endNpcIds, List<Integer> startNpcIds, List<String> zones, int killAmount, int minRank, int levelDiff,
		int startDistanceNpc) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(startNpcIds);
		if (zones != null) {
			this.zones.addAll(zones);
		} else {
			for (ZoneTemplate template : DataManager.ZONE_DATA.zoneList)
				this.zones.add(template.getXmlName());
		}
		if (killAmount == 0)
			this.killAmount = 1;
		else
			this.killAmount = killAmount;
		this.minRank = minRank;
		this.levelDiff = levelDiff;
		this.startDistanceNpc = startDistanceNpc;
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
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
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
				if (isDataDriven && dialogActionId == USE_OBJECT)
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
		if (qs == null || qs.isStartable()) {
			QuestService.startQuest(env);
			return true;
		}
		return false;
	}
}
