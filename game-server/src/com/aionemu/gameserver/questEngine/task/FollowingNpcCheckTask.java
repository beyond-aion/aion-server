package com.aionemu.gameserver.questEngine.task;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.task.checker.DestinationChecker;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer
 */
public class FollowingNpcCheckTask implements Runnable {

	private final QuestEnv env;
	private final DestinationChecker destinationChecker;

	/**
	 * @param player
	 * @param npc
	 * @param destinationChecker
	 */
	FollowingNpcCheckTask(QuestEnv env, DestinationChecker destinationChecker) {
		this.env = env;
		this.destinationChecker = destinationChecker;
	}

	@Override
	public void run() {
		final Player player = env.getPlayer();
		Npc npc = (Npc) destinationChecker.getFollower();
		if (player.isDead() || npc.isDead()) {
			onFail(env);
		}
		if (!PositionUtil.isInRange(player, npc, 50)) {
			onFail(env);
		}

		if (destinationChecker.check()) {
			onSuccess(env);
		}
	}

	/**
	 * Following task succeeded, proceed with quest
	 */
	private final void onSuccess(QuestEnv env) {
		stopFollowing(env);
		QuestEngine.getInstance().onNpcReachTarget(env);
	}

	/**
	 * Following task failed, abort further progress
	 */
	protected void onFail(QuestEnv env) {
		stopFollowing(env);
		QuestEngine.getInstance().onNpcLostTarget(env);
	}

	private final void stopFollowing(QuestEnv env) {
		Player player = env.getPlayer();
		Npc npc = (Npc) destinationChecker.getFollower();
		player.getController().cancelTask(TaskId.QUEST_FOLLOW);
		npc.getAi().onCreatureEvent(AIEventType.STOP_FOLLOW_ME, player);
		if (!npc.getAi().getName().equals("following"))
			npc.getController().delete();
	}
}