package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Majka, Bobobear
 * @modified Pad
 */

public class ReportOnLevelUp extends QuestHandler {

	private final Set<Integer> endNpcIds = new HashSet<>();

	/**
	 * @param id
	 * @param endNpcIds
	 */
	public ReportOnLevelUp(int questId, List<Integer> endNpcIds) {
		super(questId);
		if (endNpcIds != null) {
			this.endNpcIds.addAll(endNpcIds);
		}
	}

	@Override
	public void register() {
		for (Integer endNpcId : endNpcIds)
			qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);

		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();

		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId))
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		return startQuest(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return startQuest(env);
	}

	private boolean startQuest(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			env.setQuestId(questId);
			env.setPlayer(player);
			return QuestService.startQuest(env, QuestStatus.REWARD, false);
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(endNpcIds);
		}
		return constantSpawns;
	}
}
