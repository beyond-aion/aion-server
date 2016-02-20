package com.aionemu.gameserver.instance.handlers;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public interface InstanceHandler {

	/**
	 * Executed during instance creation.<br>
	 * This method will run after spawns are loaded
	 * 
	 * @param instance
	 *          created
	 */
	void onInstanceCreate(WorldMapInstance instance);

	/**
	 * Executed during instance destroy.<br>
	 * This method will run after all spawns unloaded.<br>
	 * All class-shared objects should be cleaned in handler
	 */
	void onStartInstanceDestroy();

	void onInstanceDestroy();

	void onPlayerLogin(Player player);

	void onPlayerLogOut(Player player);

	void onEnterInstance(Player player);

	void onLeaveInstance(Player player);

	void onOpenDoor(int door);

	void onEnterZone(Player player, ZoneInstance zone);

	void onEnterZone(Npc npc, ZoneInstance zone);

	void onLeaveZone(Player player, ZoneInstance zone);

	void onPlayMovieEnd(Player player, int movieId);

	boolean onReviveEvent(Player player);

	void onExitInstance(Player player);

	void doReward(Player player);

	boolean onDie(Player player, Creature lastAttacker);

	void onStopTraining(Player player);

	void onDie(Npc npc);

	void onChangeStage(StageType type);

	void onChangeStageList(StageList list);

	StageType getStage();

	void onDropRegistered(Npc npc);

	void onGather(Player player, Gatherable gatherable);

	InstanceReward<?> getInstanceReward();

	boolean onPassFlyingRing(Player player, String flyingRing);

	void handleUseItemFinish(Player player, Npc npc);

	void onLeaveTeam(Player player);

	void onHealMember(Player effector, Player effected, int value);

	void onBaseCapture(Player player);

	void onAddLegionMember(Legion legion, Player player);

	void onAddAp(Player player, int value);

	boolean canUseSkill(Player player, Skill skill);

	void onAttack(Creature attacked, Creature attacker, int damage);

	void onAggro(Npc npc);

	void onApplyEffect(Creature effector, Creature effected, int skillId);

	void onEndEffect(Creature effector, Creature effected, int skillId);
	
	void onCreatureDetected(Npc detector, Creature detected);
	
	void onSpecialEvent(Npc npc);

	boolean canEnter(Player player);

}
