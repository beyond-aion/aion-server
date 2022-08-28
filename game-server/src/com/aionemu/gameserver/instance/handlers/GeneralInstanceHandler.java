package com.aionemu.gameserver.instance.handlers;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public class GeneralInstanceHandler implements InstanceHandler {

	protected static final Logger log = LoggerFactory.getLogger("INSTANCE_LOG");
	protected final WorldMapInstance instance;
	protected final int mapId;

	public GeneralInstanceHandler(WorldMapInstance instance) {
		this.instance = instance;
		this.mapId = instance.getMapId();
	}

	@Override
	public void onInstanceCreate() {
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
		SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(mapId, npcId, x, y, z, heading);
		return SpawnEngine.spawnObject(template, instance.getInstanceId());
	}

	protected VisibleObject spawn(int npcId, float x, float y, float z, byte heading, int staticId) {
		SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(mapId, npcId, x, y, z, heading);
		template.setStaticId(staticId);
		return SpawnEngine.spawnObject(template, instance.getInstanceId());
	}

	protected VisibleObject spawnAndSetRespawn(int npcId, float x, float y, float z, byte heading, int respawnTime) {
		SpawnTemplate template = SpawnEngine.newSpawn(mapId, npcId, x, y, z, heading, respawnTime);
		return SpawnEngine.spawnObject(template, instance.getInstanceId());
	}

	protected Npc getNpc(int npcId) {
		return instance.getNpc(npcId);
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
	public void onDespawn(Npc npc) {
		if (npc.getPosition().isInstanceMap() && isBoss(npc) && !npc.isDead())
			logNpcWithReason(npc, "despawned without dying.");
	}

	@Override
	public void onDie(Npc npc) {
		if (npc.getPosition().isInstanceMap() && isBoss(npc))
			logNpcWithReason(npc, "was killed.");
	}

	public void logNpcWithReason(Npc npc, String reason) {
		log.info("[{}] {} (ID:{}) {} Involved player(s): {}", DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getName(), npc.getName(), npc.getNpcId(),
			reason,
			instance.getPlayersInside().stream().map(p -> String.format("%s (ID:%d)", p.getName(), p.getObjectId())).collect(Collectors.joining(", ")));
	}

	public boolean isBoss(Npc npc) {
		return npc.getLevel() >= 60 && (npc.getRating() == NpcRating.HERO || npc.getRating() == NpcRating.LEGENDARY);
	}

	@Override
	public void onSpawn(VisibleObject object) {
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
	public void onDropRegistered(Npc npc, int winnerObj) {
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
	public void onStartEffect(Effect effect) {

	}

	@Override
	public void onEndEffect(Effect effect) {

	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {

	}

	@Override
	public void onSpecialEvent(Npc npc) {

	}

	@Override
	public void onBackHome(Npc npc) {

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
