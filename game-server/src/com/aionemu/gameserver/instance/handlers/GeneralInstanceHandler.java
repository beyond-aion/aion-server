package com.aionemu.gameserver.instance.handlers;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldType;
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
	public void onPlayerLogout(Player player) {
	}

	@Override
	public void onEnterInstance(Player player) {
	}

	@Override
	public void leaveInstance(Player player) {
	}

	@Override
	public void onLeaveInstance(Player player) {
		player.getEffectController().removeInstanceEffects();
		removeInstanceItems(player);
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

	protected void deleteAliveNpcs(int... npcIds) {
		instance.getNpcs(npcIds).forEach(n -> n.getController().deleteIfAliveOrCancelRespawn());
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
		if (!instance.getPlayersInside().isEmpty()) {
			log.info("[{}] {} (ID:{}) {} Player(s) in instance: {}", DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getName(), npc.getName(),
				npc.getNpcId(), reason,
				instance.getPlayersInside().stream().map(p -> String.format("%s (ID:%d)", p.getName(), p.getObjectId())).collect(Collectors.joining(", ")));
		}
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
	public InstanceScore<?> getInstanceScore() {
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
	public void onEndCastSkill(Skill skill) {
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
	public void portToStartPosition(Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canEnter(Player player) {
		return true;
	}

	@Override
	public float getExpMultiplier() {
		return instance.getParent().isInstanceType() ? 1.5f : 1.25f; // on retail, instances reward more exp than regular world maps
	}

	@Override
	public float getApMultiplier() {
		return 1f;
	}

	@Override
	public boolean allowSelfReviveBySkill() {
		return true; // see skill_prohibit_set_id in client /data/world/worldid.xml + data/skills/client_skill_prohibit.xml
	}

	@Override
	public boolean allowSelfReviveByItem() {
		return true;
	}

	@Override
	public boolean allowKiskRevive() {
		return !instance.getTemplate().isInstance();
	}

	@Override
	public boolean allowInstanceRevive() {
		return instance.getTemplate().isInstance() && getClass() != GeneralInstanceHandler.class || instance.getTemplate().getWorldType() == WorldType.PANESTERRA;
	}

	protected boolean isRestrictedToInstance(Item item) {
		return item.getItemTemplate().isItemRestrictedToWorld(instance.getMapId());
	}

	private void removeInstanceItems(Player player) {
		for (Item item : player.getInventory().getItems())
			if (isRestrictedToInstance(item))
				player.getInventory().decreaseByObjectId(item.getObjectId(), item.getItemCount());
		for (Storage storage : player.getPetBags()) {
			for (Item item : storage.getItems())
				if (isRestrictedToInstance(item))
					storage.decreaseByObjectId(item.getObjectId(), item.getItemCount());
		}
	}
}
