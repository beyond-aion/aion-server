package com.aionemu.gameserver.custom.pvpmap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BIND_POINT_TELEPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PvpService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.BindPointTeleportService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.spawnengine.StaticDoorSpawnManager;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * Created on 06.04.2016.
 * 
 * @author Yeats
 */
public class PvpMapHandler extends GeneralInstanceHandler {

	private static final int SHUGO_SPAWN_RATE = 30;
	private final Map<Integer, WorldPosition> origins = new FastMap<>();
	private final Map<Integer, Long> joinOrLeaveTime = new FastMap<>();
	private final List<WorldPosition> respawnLocations = new FastTable<>();
	private final List<WorldPosition> supplyPositions = new FastTable<>();
	private final AtomicBoolean canJoin = new AtomicBoolean();
	private Future<?> supplyTask, despawnTask, observationTask;

	public PvpMapHandler() {
		super();
		InstanceService.getNextAvailableInstance(301220000, 0, (byte) 0, this);
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		StaticDoorSpawnManager.spawnTemplate(mapId, instanceId);
		for (StaticDoor door : instance.getDoors().values()) {
			door.setOpen(true);
		}
		addRespawnLocations();
		startSupplyTask();
		canJoin.set(true);
	}

	private void spawnShugo(Player player) {
		if (canJoin.get() && Rnd.chance() < SHUGO_SPAWN_RATE) {
			Npc oldShugo = instance.getNpc(833543);
			if (oldShugo != null) {
				oldShugo.getController().delete();
			}
			double radian = Math.toRadians(MathUtil.convertHeadingToDegree(player.getHeading()));
			float x = player.getX() + (float) (Math.cos(radian) * 2);
			float y = player.getY() + (float) (Math.sin(radian) * 2);
			float z = GeoService.getInstance().getZ(player.getWorldId(), x, y, player.getZ(), 0.5f, instanceId);
			spawn(833543, x, y, z, MathUtil.getHeadingTowards(x, y, player.getX(), player.getY()));
		}
	}

	// spawns a supply chest every ~6min if there are enough players on the map
	private void startSupplyTask() {
		supplyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::scheduleSupplySpawn, 120000, 400000);
	}

	private void scheduleSupplySpawn() {
		if (spawnAllowed()) {
			if (supplyPositions.isEmpty()) {
				addSupplyPositions();
			}
			final WorldPosition pos = supplyPositions.get(Rnd.get(0, supplyPositions.size() - 1));
			supplyPositions.remove(pos);
			spawn(831980, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading()); // flag
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GUARDLIGHTHERO_SPAWN_IDLDF5_UNDER_01_WAR(), 0);
			scheduleDespawns();
			ThreadPoolManager.getInstance().schedule((Runnable) () -> {
				spawn(233192, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading()); // chest
			}, 30000);
		}
	}

	private void scheduleDespawns() {
		despawnTask = ThreadPoolManager.getInstance().schedule(
			() -> instance.getNpcs().stream().filter(npc -> !npc.isInState(CreatureState.DEAD)).forEach(npc -> npc.getController().delete()), 120000);
	}

	public void join(Player p) {
		if (canJoin(p)) {
			startTeleportation(p, false);
		}
	}

	private void startTeleportation(Player p, boolean isLeaving) {
		ActionObserver observer = getAllObserver(p);
		PacketSendUtility.broadcastPacket(p, new SM_BIND_POINT_TELEPORT(1, p.getObjectId(), 1, 0), true);
		p.getObserveController().attach(observer);

		p.getController().addTask(TaskId.SKILL_USE, ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastPacket(p, new SM_BIND_POINT_TELEPORT(3, p.getObjectId(), 1, 0), true);
			ThreadPoolManager.getInstance().schedule(() -> {
				p.getObserveController().removeObserver(observer);
				p.getController().cancelTask(TaskId.SKILL_USE);
				if (!p.getController().isInCombat() && !p.getLifeStats().isAboutToDie() && !p.getLifeStats().isAlreadyDead()) {
					if (isLeaving) {
						removePlayer(p);
					} else {
						updateOrigin(p);
						updateJoinOrLeaveTime(p);
						InstanceService.registerPlayerWithInstance(instance, p);
						WorldPosition pos = respawnLocations.get(Rnd.get(0, respawnLocations.size() - 1));
						TeleportService2.teleportTo(p, pos.getMapId(), instanceId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(),
							TeleportAnimation.BATTLEGROUND);
					}
				}
			}, 1000);
		}, 10000));
	}

	private ActionObserver getAllObserver(final Player p) {
		return new ItemUseObserver() {

			@Override
			public void abort() {
				BindPointTeleportService.cancelTeleport(p, 1);
			}
		};
	}

	private boolean canJoin(Player p) {
		if (p.isGM()) {
			return true;
		} else if (!canJoin.get() || p.getController().hasScheduledTask(TaskId.SKILL_USE)) {
			PacketSendUtility.sendMessage(p, "You cannot enter the PvP-Map now.");
			return false;
		} else if (!checkState(p)) {
			PacketSendUtility.sendMessage(p, "You cannot enter the PvP-Map in your current state.");
			return false;
		} else if (joinOrLeaveTime.containsKey(p.getObjectId()) && ((System.currentTimeMillis() - joinOrLeaveTime.get(p.getObjectId())) < 120000)) {
			int timeInSeconds = (int) Math.ceil((120000 - (System.currentTimeMillis() - joinOrLeaveTime.get(p.getObjectId()))) / 1000f);
			PacketSendUtility.sendMessage(p, "You can reenter the PvP-Map in " + timeInSeconds + " second" + (timeInSeconds > 1 ? "s." : "."));
			return false;
		} else {
			return true;
		}
	}

	private boolean checkState(Player p) {
		return !p.getController().isInCombat() && !p.getLifeStats().isAboutToDie() && !p.getLifeStats().isAlreadyDead() && !p.isLooting()
			&& !p.isInGlidingState() && !p.isFlying() && !p.isUsingFlyTeleport() && !p.isInPlayerMode(PlayerMode.WINDSTREAM)
			&& !p.isInPlayerMode(PlayerMode.RIDE) && !p.hasStore() && p.getCastingSkill() == null
			&& !p.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE)
			&& !p.getEffectController().isInAnyAbnormalState(AbnormalState.ROOT);
	}

	private synchronized void updateOrigin(Player p) {
		origins.put(p.getObjectId(), p.getPosition());
	}

	private synchronized void updateJoinOrLeaveTime(Player p) {
		if (!p.isGM())
			joinOrLeaveTime.put(p.getObjectId(), System.currentTimeMillis());
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 100, 100, false, 0);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		player.unsetResPosState();

		if (!canJoin.get() || respawnLocations.isEmpty()) {
			if (instance.getPlayer(player.getObjectId()) != null) {
				removePlayer(player);
			}
		} else {
			WorldPosition pos = respawnLocations.get(Rnd.get(respawnLocations.size()));
			TeleportService2.teleportTo(player, pos.getMapId(), instanceId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(),
				TeleportAnimation.BATTLEGROUND);
		}
		return true;
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		if (canJoin.get()) {
			if (lastAttacker instanceof Player && !lastAttacker.equals(player)) {
				spawnShugo((Player) lastAttacker);
			}
			PvpService.getInstance().doReward(player, CustomConfig.PVP_MAP_AP_MULTIPLIER);
			announceDeath(player);
			PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 6));
		}
		return true;
	}

	private void announceDeath(final Player player) {
		if (!player.isGM() && player.getAbyssRank() != null) {
			int zoneNameId = getZoneNameId(player);
			if (zoneNameId > 0)
				PacketSendUtility.broadcastToMap(instance,
					SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(player, AbyssRankEnum.getRankDescriptionId(player), zoneNameId));
			else
				PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(player, AbyssRankEnum.getRankDescriptionId(player)));
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!player.isGM()) {
			updateJoinOrLeaveTime(player);
			instance.forEachPlayer(p -> {
				if (!p.equals(player))
					PacketSendUtility.sendMessage(p, "A new player has joined!", ChatType.BRIGHT_YELLOW_CENTER);
			});
			PacketSendUtility.broadcastToWorld(new SM_MESSAGE(0, null, "An enemy has entered the PvP-Map!", ChatType.BRIGHT_YELLOW_CENTER),
				p -> p.getLevel() >= 60 && !p.isInInstance() && p.getRace() != player.getRace());
		}
	}

	@Override
	public void onLeaveInstance(Player player) {
		updateJoinOrLeaveTime(player);
	}

	@Override
	public void onPlayerLogin(Player player) {
		updateJoinOrLeaveTime(player);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		updateJoinOrLeaveTime(player);
		removePlayer(player);
	}

	@Override
	public void onInstanceDestroy() {
		PvpMapService.getInstance().closeMap(instanceId);
		canJoin.set(false);
		cancelTasks();
		clearLists();
	}

	private void cancelTasks() {
		if (supplyTask != null && !supplyTask.isCancelled()) {
			supplyTask.cancel(true);
		}
		if (despawnTask != null && !despawnTask.isCancelled()) {
			despawnTask.cancel(true);
		}
		if (observationTask != null && !observationTask.isCancelled()) {
			observationTask.cancel(true);
		}
	}

	private boolean spawnAllowed() {
		if (!canJoin.get())
			return false;
		byte asmodians = 0;
		byte elyos = 0;
		for (Player player : instance.getPlayersInside()) {
			if (player.isGM()) {
				continue;
			} else if (player.getRace() == Race.ASMODIANS) {
				asmodians++;
			} else {
				elyos++;
			}
			if (asmodians > 1 && elyos > 1) {
				return true;
			}
		}
		return false;
	}

	public int getParticipantsSize() {
		int playerCount = 0;
		for (Player p : instance.getPlayersInside()) {
			if (!p.isGM()) {
				playerCount++;
			}
		}
		return playerCount;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void removeAllPlayersAndStop() {
		canJoin.set(false);
		cancelTasks();
		instance.forEachPlayer(this::removePlayer);
		clearLists();
	}

	private void clearLists() {
		joinOrLeaveTime.clear();
		respawnLocations.clear();
		supplyPositions.clear();
		origins.clear();
	}

	private synchronized void removePlayer(Player p) {
		updateJoinOrLeaveTime(p);
		if (p.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.broadcastPacket(p, new SM_EMOTION(p, EmotionType.RESURRECT), true);
			PlayerReviveService.revive(p, 25, 25, true, 0);
		}
		WorldPosition position = origins.get(p.getObjectId());
		if (position != null && !SiegeService.getInstance().isNearVulnerableFortress(position.getMapId(), position.getX(), position.getY(), position.getZ())) {
			TeleportService2.teleportTo(p, position);
			origins.remove(p.getObjectId());
		} else {
			TeleportService2.moveToBindLocation(p);
		}
	}

	public boolean leave(Player p) {
		if (checkState(p) && !p.getController().hasScheduledTask(TaskId.SKILL_USE)) {
			startTeleportation(p, true);
			return true;
		}
		return false;
	}

	public boolean isOnMap(Creature creature) {
		return instance.getObject(creature.getObjectId()) != null;
	}

	private void addRespawnLocations() {
		respawnLocations.clear();
		respawnLocations.add(new WorldPosition(mapId, 606, 158, 218, (byte) 74));
		respawnLocations.add(new WorldPosition(mapId, 519, 231, 233, (byte) 0));
		respawnLocations.add(new WorldPosition(mapId, 451, 268, 247, (byte) 30));
		respawnLocations.add(new WorldPosition(mapId, 395, 271, 254, (byte) 109));
		respawnLocations.add(new WorldPosition(mapId, 283, 392, 239, (byte) 13));
		respawnLocations.add(new WorldPosition(mapId, 355, 491, 240, (byte) 92));
		respawnLocations.add(new WorldPosition(mapId, 424, 507, 230, (byte) 11));
		respawnLocations.add(new WorldPosition(mapId, 424, 642, 216, (byte) 86));
		respawnLocations.add(new WorldPosition(mapId, 483, 675, 222, (byte) 36));
		respawnLocations.add(new WorldPosition(mapId, 425, 770, 206, (byte) 118));
		respawnLocations.add(new WorldPosition(mapId, 606, 893, 197, (byte) 91));
		respawnLocations.add(new WorldPosition(mapId, 589, 705, 222, (byte) 73));
		respawnLocations.add(new WorldPosition(mapId, 710, 632, 213, (byte) 36));
		respawnLocations.add(new WorldPosition(mapId, 788, 772, 202, (byte) 69));
		respawnLocations.add(new WorldPosition(mapId, 825, 559, 240, (byte) 79));
		respawnLocations.add(new WorldPosition(mapId, 663, 548, 240, (byte) 90));
		respawnLocations.add(new WorldPosition(mapId, 587, 505, 219, (byte) 99));
		respawnLocations.add(new WorldPosition(mapId, 566, 430, 224, (byte) 16));
		respawnLocations.add(new WorldPosition(mapId, 450, 449, 272, (byte) 74));
		respawnLocations.add(new WorldPosition(mapId, 657, 394, 241, (byte) 103));
		respawnLocations.add(new WorldPosition(mapId, 631, 265, 239, (byte) 64));
		respawnLocations.add(new WorldPosition(mapId, 636, 321, 227, (byte) 113));
		respawnLocations.add(new WorldPosition(mapId, 700, 346, 230, (byte) 12));
		respawnLocations.add(new WorldPosition(mapId, 758, 356, 233, (byte) 44));
		respawnLocations.add(new WorldPosition(mapId, 780, 392, 245, (byte) 19));
		respawnLocations.add(new WorldPosition(mapId, 781, 319, 254, (byte) 71));
		respawnLocations.add(new WorldPosition(mapId, 708, 262, 254, (byte) 12));
		respawnLocations.add(new WorldPosition(mapId, 749, 327, 235, (byte) 89));
		respawnLocations.add(new WorldPosition(mapId, 708, 292, 235, (byte) 118));
	}

	private void addSupplyPositions() {
		supplyPositions.clear();
		supplyPositions.add(new WorldPosition(mapId, 522.168f, 538.0537f, 214.49342f, (byte) 73));
		supplyPositions.add(new WorldPosition(mapId, 251.04434f, 757.9979f, 201.26372f, (byte) 114));
		supplyPositions.add(new WorldPosition(mapId, 271.27472f, 553.897f, 231.08957f, (byte) 44));
		supplyPositions.add(new WorldPosition(mapId, 461.37543f, 546.35223f, 217.90244f, (byte) 28));
		supplyPositions.add(new WorldPosition(mapId, 485.3683f, 436.35458f, 229.98183f, (byte) 119));
		supplyPositions.add(new WorldPosition(mapId, 618.5215f, 361.99954f, 224.94342f, (byte) 46));
		supplyPositions.add(new WorldPosition(mapId, 640.56665f, 413.10803f, 243.93953f, (byte) 104));
		supplyPositions.add(new WorldPosition(mapId, 716.21234f, 323.26202f, 233.50352f, (byte) 103));
		supplyPositions.add(new WorldPosition(mapId, 661.0137f, 239.082f, 232.08531f, (byte) 74));
		supplyPositions.add(new WorldPosition(mapId, 782.2936f, 376.2715f, 237.75659f, (byte) 96));
		supplyPositions.add(new WorldPosition(mapId, 807.1606f, 605.07587f, 239.5659f, (byte) 89));
		supplyPositions.add(new WorldPosition(mapId, 617.14386f, 590.6163f, 207.76883f, (byte) 110));
		supplyPositions.add(new WorldPosition(mapId, 595.5325f, 782.74634f, 186.63377f, (byte) 85));
		supplyPositions.add(new WorldPosition(mapId, 569.0313f, 253.5052f, 232.96907f, (byte) 89));
		supplyPositions.add(new WorldPosition(mapId, 810.50385f, 465.51343f, 229.0315f, (byte) 51));
		supplyPositions.add(new WorldPosition(mapId, 685.11664f, 427.93045f, 229.82187f, (byte) 31));
		supplyPositions.add(new WorldPosition(mapId, 376.81177f, 364.2379f, 225.97026f, (byte) 57));
		supplyPositions.add(new WorldPosition(mapId, 676.6278f, 721.29083f, 178.93533f, (byte) 105));
		supplyPositions.add(new WorldPosition(mapId, 539.07605f, 687.2986f, 205.5f, (byte) 26));
	}

	private int getZoneNameId(Player player) {
		List<ZoneInstance> zones = player.getPosition().getMapRegion().getZones(player);
		if (zones.isEmpty()) {
			return 0;
		} else {
			for (ZoneInstance zone : zones) {
				if (!zone.getAreaTemplate().getZoneName().name().equalsIgnoreCase("301220000")) {
					return getZoneNameIdByZoneName(zone.getAreaTemplate().getZoneName().name());
				}
			}
			return 0;
		}
	}

	private int getZoneNameIdByZoneName(String name) {
		switch (name) {
			case "ANCILLARY_SENTRY_POST_301220000":
				return 404085;
			case "ARTILLERY_COMMAND_CENTER_301220000":
				return 404088;
			case "ASSAULT_COMMAND_CENTER_301220000":
				return 404090;
			case "AXIAL_SENTRY_POST_301220000":
				return 404084;
			case "CENTRAL_SUPPLY_BASE_301220000":
				return 404086;
			case "HEADQUARTERS_301220000":
				return 404092;
			case "HEADQUARTERS_ANNEX_301220000":
				return 404093;
			case "HOLY_GROUND_OF_RESURRECTION_301220000":
				return 404094;
			case "MILITARY_SUPPLY_BASE_2_301220000":
				return 404089;
			case "PASHID_ARMY_ENCAMPMENT_301220000":
				return 404083;
			case "PERIPHERAL_SUPPLY_BASE_301220000":
				return 404087;
			case "SIEGE_BASE_301220000":
				return 404091;
			case "THE_ETERNAL_BASTION_301220000":
				return 404082;
			case "UNDERGROUND_WATERWAY_1_301220000":
				return 404095;
			default:
				return 0;
		}
	}
}
