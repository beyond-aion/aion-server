package com.aionemu.gameserver.instance.handlers;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public interface InstanceHandler {

	/**
	 * Executed during instance creation.<br>
	 * This method will run after spawns are loaded
	 */
	void onInstanceCreate();

	/**
	 * Executed during instance destroy.<br>
	 * This method will run after all spawns unloaded.<br>
	 * All class-shared objects should be cleaned in handler
	 */
	void onInstanceDestroy();

	void onPlayerLogin(Player player);

	void onPlayerLogout(Player player);

	void onEnterInstance(Player player);

	void leaveInstance(Player player);

	void onLeaveInstance(Player player);

	void onOpenDoor(int door);

	void onEnterZone(Player player, ZoneInstance zone);

	void onLeaveZone(Player player, ZoneInstance zone);

	void onPlayMovieEnd(Player player, int movieId);

	boolean onReviveEvent(Player player);

	void doReward(Player player);

	boolean onDie(Player player, Creature lastAttacker);

	void onStopTraining(Player player);

	void onDespawn(Npc npc);

	void onDie(Npc npc);

	void onSpawn(VisibleObject obj);

	void onChangeStage(StageType type);

	void onChangeStageList(StageList list);

	StageType getStage();

	void onDropRegistered(Npc npc, int winnerObj);

	void onGather(Player player, Gatherable gatherable);

	InstanceScore<?> getInstanceScore();

	boolean onPassFlyingRing(Player player, String flyingRing);

	void handleUseItemFinish(Player player, Npc npc);

	void onEndCastSkill(Skill skill);

	void onAggro(Npc npc);

	void onStartEffect(Effect effect);

	void onEndEffect(Effect effect);

	void onCreatureDetected(Npc detector, Creature detected);

	void onSpecialEvent(Npc npc);

	void onBackHome(Npc npc);

	void portToStartPosition(Player player);

	boolean canEnter(Player player);

	float getExpMultiplier();

	float getApMultiplier();

	boolean allowSelfReviveBySkill();
	boolean allowSelfReviveByItem();
	boolean allowKiskRevive();
	boolean allowInstanceRevive();
}
