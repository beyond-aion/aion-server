package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Majka, Bobobear
 */

public class ReportOnLevelUp extends QuestHandler {

	private final Set<Integer> endNpcs = new HashSet<>();
	@SuppressWarnings("unused")
	private final int endDialog;

	/**
	 * @param id
	 * @param endNpcIds
	 * @param endDialog
	 */
	public ReportOnLevelUp(int questId, List<Integer> endNpcIds, int endDialog) {
		super(questId);
		if (endNpcIds != null) {
			endNpcs.addAll(endNpcIds);
			endNpcs.remove(0);
		}
		this.endDialog = endDialog;
	}

	@Override
	public void register() {
		for (Integer endNpc : endNpcs)
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());

		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(getQuestId());

		if (qs == null)
			return false;

		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcs.contains(targetId)) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (endNpcs != null)
				constantSpawns.addAll(endNpcs);
		}
		return constantSpawns;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null && player.getLevel() >= DataManager.QUEST_DATA.getQuestById(env.getQuestId()).getMinlevelPermitted()) {
			env.setQuestId(questId);
			env.setPlayer(player);
			if (QuestService.startQuest(env, QuestStatus.REWARD))
				return true;
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null && player.getLevel() >= DataManager.QUEST_DATA.getQuestById(env.getQuestId()).getMinlevelPermitted()) {
			env.setQuestId(questId);
			env.setPlayer(player);
			if (QuestService.startQuest(env, QuestStatus.REWARD))
				return true;
		}
		return false;
	}
}
