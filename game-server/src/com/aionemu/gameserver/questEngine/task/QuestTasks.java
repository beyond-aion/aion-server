package com.aionemu.gameserver.questEngine.task;

import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.task.checker.CoordinateDestinationChecker;
import com.aionemu.gameserver.questEngine.task.checker.TargetDestinationChecker;
import com.aionemu.gameserver.questEngine.task.checker.ZoneChecker;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author ATracer
 */
public class QuestTasks {

	/**
	 * Schedule new following checker task
	 * 
	 * @param player
	 * @param npc
	 * @param target
	 * @return
	 */
	public static final Future<?> newFollowingToTargetCheckTask(final QuestEnv env, Npc npc, Npc target) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FollowingNpcCheckTask(env, new TargetDestinationChecker(npc, target)), 1000, 1000);
	}

	/**
	 * Schedule new following checker task
	 * 
	 * @param player
	 * @param npc
	 * @param npcTargetId
	 * @return
	 */
	public static final Future<?> newFollowingToTargetCheckTask(final QuestEnv env, Npc npc, int npcTargetId) {
		SpawnSearchResult searchResult = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(npc.getWorldId(), npcTargetId);
		if (searchResult == null) {
			throw new IllegalArgumentException("Supplied npc doesn't exist: " + npcTargetId);
		}
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(
			new FollowingNpcCheckTask(env, new CoordinateDestinationChecker(npc, searchResult.getSpot().getX(), searchResult.getSpot().getY(), searchResult
				.getSpot().getZ())), 1000, 1000);
	}

	/**
	 * Schedule new following checker task
	 * 
	 * @param env
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static final Future<?> newFollowingToTargetCheckTask(final QuestEnv env, Npc npc, float x, float y, float z) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FollowingNpcCheckTask(env, new CoordinateDestinationChecker(npc, x, y, z)), 1000,
			1000);
	}

	public static final Future<?> newFollowingToTargetCheckTask(final QuestEnv env, Npc npc, ZoneName zoneName) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FollowingNpcCheckTask(env, new ZoneChecker(npc, zoneName)), 1000, 1000);
	}
}
