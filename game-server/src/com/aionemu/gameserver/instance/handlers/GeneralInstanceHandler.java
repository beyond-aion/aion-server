package com.aionemu.gameserver.instance.handlers;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public class GeneralInstanceHandler implements InstanceHandler {

	protected final long creationTime;
	protected WorldMapInstance instance;
	protected int instanceId;
	protected Integer mapId;

	public GeneralInstanceHandler() {
		creationTime = System.currentTimeMillis();
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		this.instance = instance;
		this.instanceId = instance.getInstanceId();
		this.mapId = instance.getMapId();
	}

	@Override
	public void onInstanceDestroy() {
	}

	@Override
	public void onPlayerLogin(Player player) {
	}

	@Override
	public void onPlayerLogOut(Player player) {
	}

	@Override
	public void onEnterInstance(Player player) {
	}

	@Override
	public void onLeaveInstance(Player player) {
	}

	@Override
	public void onOpenDoor(int door) {
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
	}

	@Override
	public void onLeaveZone(Player player, ZoneInstance zone) {
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
	}

	@Override
	public boolean onReviveEvent(Player player) {
		return false;
	}

	protected VisibleObject spawn(int npcId, float x, float y, float z, byte heading) {
		SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, heading);
		return SpawnEngine.spawnObject(template, instanceId);
	}

	protected VisibleObject spawn(int npcId, float x, float y, float z, byte heading, int staticId) {
		SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, heading);
		template.setStaticId(staticId);
		return SpawnEngine.spawnObject(template, instanceId);
	}

	protected VisibleObject spawnAndSetRespawn(int npcId, float x, float y, float z, byte heading, int respawnTime) {
		SpawnTemplate template = SpawnEngine.addNewSpawn(mapId, npcId, x, y, z, heading, respawnTime);
		return SpawnEngine.spawnObject(template, instanceId);
	}

	protected Npc getNpc(int npcId) {
		return instance.getNpc(npcId);
	}

	/**
	 * Sends a message to all players in this instance.
	 */
	protected void sendMsg(int msg) {
		sendMsg(new SM_SYSTEM_MESSAGE(ChatType.GOLDEN_YELLOW, null, msg));
	}

	/**
	 * Sends a message to all players in this instance.
	 */
	protected void sendMsg(int msg, int delay) {
		sendMsg(new SM_SYSTEM_MESSAGE(ChatType.GOLDEN_YELLOW, null, msg), delay);
	}

	/**
	 * Sends a message to all players in this instance.
	 */
	protected void sendMsg(SM_SYSTEM_MESSAGE msg) {
		sendMsg(msg, 0);
	}

	/**
	 * Sends a message to all players in this instance, after the specified delay (in milliseconds).
	 */
	protected void sendMsg(SM_SYSTEM_MESSAGE msg, int delay) {
		PacketSendUtility.broadcastToMap(instance, msg, delay);
	}
	
	@Override
	public float getInstanceExpMultiplier() {
		return instance != null && instance.getParent().isInstanceType() ? 1.5f : 1.25f; // instance maps * 1.5, world maps * 1.25
	}

	@Override
	public void onExitInstance(Player player) {
	}

	@Override
	public void doReward(Player player) {
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		return false;
	}

	@Override
	public void onStopTraining(Player player) {
	}

	@Override
	public void onDie(Npc npc) {
	}

	@Override
	public void onAggro(Npc npc) {
	}

	@Override
	public void onChangeStage(StageType type) {
	}

	@Override
	public void onChangeStageList(StageList list) {
	}

	@Override
	public StageType getStage() {
		return StageType.DEFAULT;
	}

	@Override
	public void onDropRegistered(Npc npc) {
	}

	@Override
	public void onGather(Player player, Gatherable gatherable) {
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return null;
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		return false;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
	}

	@Override
	public boolean canUseSkill(Player player, Skill skill) {
		return true;
	}

	@Override
	public void onEndEffect(Creature effector, Creature effected, int skillId) {

	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {

	}

	@Override
	public void onSpecialEvent(Npc npc) {

	}

	@Override
	public boolean canEnter(Player player) {
		return true;
	}

	@Override
	public float getInstanceApMultiplier() {
		return 1f;
	}
}
