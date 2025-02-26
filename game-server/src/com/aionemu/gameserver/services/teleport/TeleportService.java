package com.aionemu.gameserver.services.teleport;

import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.animations.ArrivalAnimation;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.portal.InstanceExit;
import com.aionemu.gameserver.model.templates.portal.PortalLoc;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.portal.PortalScroll;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.teleport.TelelocationTemplate;
import com.aionemu.gameserver.model.templates.teleport.TeleportLocation;
import com.aionemu.gameserver.model.templates.teleport.TeleportType;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.PrivateStoreService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author xTz, Neon
 */
public class TeleportService {

	private static final Logger log = LoggerFactory.getLogger(TeleportService.class);
	private static double[] eventPosAsmodians;
	private static double[] eventPosElyos;

	/**
	 * Performs flight teleportation
	 */
	public static void teleport(TeleporterTemplate template, int locId, Player player, Npc npc, TeleportAnimation animation) {
		TribeClass tribe = npc.getTribe();
		Race race = player.getRace();
		if (tribe.equals(TribeClass.FIELD_OBJECT_LIGHT) && race.equals(Race.ASMODIANS)
			|| (tribe.equals(TribeClass.FIELD_OBJECT_DARK) && race.equals(Race.ELYOS))) {
			return;
		}

		TeleportLocation location = template.getTeleLocIdData() == null ? null : template.getTeleLocIdData().getTeleportLocation(locId);
		if (location == null) {
			log.warn("Missing location in npc_teleporter.xml for locId {} (npc {})", locId, npc.getNpcId());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			return;
		}

		TelelocationTemplate locationTemplate = DataManager.TELELOCATION_DATA.getTelelocationTemplate(locId);
		if (locationTemplate == null) {
			log.warn("Missing teleloc_template in teleport_location.xml with locId {}", locId);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			return;
		}

		// TODO: remove teleportation route if it's enemy fortress (1221, 1231, 1241)
		int id = SiegeService.getInstance().getSiegeIdByLocId(locId);
		if (id > 0 && !SiegeService.getInstance().getSiegeLocation(id).isCanTeleport(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			return;
		}

		if (!checkKinahForTransportation(location, player))
			return;

		if (location.getType().equals(TeleportType.FLIGHT)) {
			if (SecurityConfig.ENABLE_FLYPATH_VALIDATOR) {
				FlyPathEntry flypath = DataManager.FLY_PATH.getPathTemplate(location.getLocId());
				if (flypath == null) {
					AuditLogger.log(player, "tried to use invalid flyPath #" + location.getLocId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				double dist = PositionUtil.getDistance(player, flypath.getStartX(), flypath.getStartY(), flypath.getStartZ());
				if (dist > 7) {
					AuditLogger.log(player, "tried to use flyPath #" + location.getLocId() + " but he's too far " + dist);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				if (player.getWorldId() != flypath.getStartWorldId()) {
					AuditLogger.log(player, "tried to use flyPath #" + location.getLocId() + " from invalid start world " + player.getWorldId()
						+ ", expected " + flypath.getStartWorldId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				player.setCurrentFlypath(flypath);
			}
			player.unsetPlayerMode(PlayerMode.RIDE);
			player.setState(CreatureState.FLYING);
			player.unsetState(CreatureState.ACTIVE);
			player.setFlightTeleportId(location.getTeleportId());
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, location.getTeleportId(), 0), true);
		} else {
			int instanceId = 1;
			int mapId = locationTemplate.getMapId();
			if (player.getWorldId() == mapId) {
				instanceId = player.getInstanceId();
			}
			sendLoc(player, mapId, instanceId, locationTemplate.getX(), locationTemplate.getY(), locationTemplate.getZ(),
				(byte) locationTemplate.getHeading(), animation);
		}
	}

	private static boolean checkKinahForTransportation(TeleportLocation location, Player player) {
		Storage inventory = player.getInventory();

		long transportationPrice;

		// If HiPassEffect is active, then all flight/teleport prices are 1 kinah
		if (player.getEffectController().hasAbnormalEffect(Effect::isHiPass))
			transportationPrice = 1;
		else {
			int basePrice = location.getPrice();
			// TODO check for location.getPricePvp()
			transportationPrice = PricesService.getPriceForService(basePrice, player.getRace());
		}

		if (!inventory.tryDecreaseKinah(transportationPrice, ItemUpdateType.DEC_KINAH_FLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(transportationPrice));
			return false;
		}
		return true;
	}

	private static void sendLoc(Player player, int worldId, int instanceId, float x, float y, float z, byte h, TeleportAnimation animation) {
		abortPlayerActions(player);
		// despawn from world and send animation to others (also ends flying)
		World.getInstance().despawn(player, animation.getDefaultObjectDeleteAnimation());

		SpawnTask spawnTask = new SpawnTask(player, worldId, instanceId, x, y, z, h, animation);
		if (animation == TeleportAnimation.NONE) // instant teleport (don't wait for player fade-out)
			spawnTask.run();
		else {
			// send teleport animation to player and trigger CM_TELEPORT_ANIMATION_DONE when the animation ended
			PacketSendUtility.sendPacket(player, new SM_TELEPORT_LOC(worldId, instanceId, x, y, z, h, animation));
			// task will be triggered from CM_TELEPORT_ANIMATION_DONE
			player.getController().addTask(TaskId.TELEPORT, new FutureTask<Void>(spawnTask, null));
		}
	}

	private static void abortPlayerActions(Player player) {
		if (player.hasStore())
			PrivateStoreService.closePrivateStore(player);
		player.getController().cancelCurrentSkill(null);
		player.setTarget(null);
		player.unsetPlayerMode(PlayerMode.RIDE);
	}

	private static void spawnOnSameMap(Player player) {
		PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
		PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		World.getInstance().spawn(player);
		World.getInstance().spawn(player.getPet());
		player.getController().startProtectionActiveTask();
		player.getEffectController().updatePlayerEffectIcons(null);
		player.getController().updateZone();
		player.setPortAnimation(ArrivalAnimation.NONE);
	}

	public static void teleportTo(Player player, WorldPosition pos) {
		if (player.getWorldId() == pos.getMapId()) {
			World.getInstance().setPosition(player.getPet(), pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
			World.getInstance().setPosition(player, pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
			spawnOnSameMap(player);
		} else if (player.isDead()) {
			teleportDeadTo(player, pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		} else {
			teleportTo(player, pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), TeleportAnimation.NONE);
		}
	}

	public static void teleportDeadTo(Player player, int worldId, int instanceId, float x, float y, float z, byte heading) {
		if (player.getWorldId() != worldId || player.getInstanceId() != instanceId) {
			ConquerorAndProtectorService.getInstance().onLeaveMap(player);
			InstanceService.onLeaveInstance(player);
		}
		World.getInstance().setPosition(player, worldId, instanceId, x, y, z, heading);
		PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
		PacketSendUtility.sendPacket(player, new SM_PLAYER_SPAWN(player));
		player.setPortAnimation(ArrivalAnimation.LANDING);
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));

		if (player.isLegionMember()) {
			PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
		}
	}

	public static void teleportTo(Player player, int worldId, float x, float y, float z) {
		teleportTo(player, worldId, x, y, z, player.getHeading(), TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, int worldId, float x, float y, float z, byte h) {
		teleportTo(player, worldId, x, y, z, h, TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, int worldId, float x, float y, float z, byte h, TeleportAnimation animation) {
		teleportTo(player, worldId, player.getWorldId() != worldId ? 1 : player.getInstanceId(), x, y, z, h, animation);
	}

	public static void teleportTo(Player player, int worldId, int instanceId, float x, float y, float z) {
		teleportTo(player, worldId, instanceId, x, y, z, player.getHeading(), TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, int worldId, int instanceId, float x, float y, float z, byte h) {
		teleportTo(player, worldId, instanceId, x, y, z, h, TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, WorldMapInstance instance, float x, float y, float z) {
		teleportTo(player, instance.getMapId(), instance.getInstanceId(), x, y, z, player.getHeading(), TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, WorldMapInstance instance, float x, float y, float z, byte h) {
		teleportTo(player, instance.getMapId(), instance.getInstanceId(), x, y, z, h, TeleportAnimation.NONE);
	}

	public static void teleportTo(Player player, WorldMapInstance instance, float x, float y, float z, byte h, TeleportAnimation animation) {
		teleportTo(player, instance.getMapId(), instance.getInstanceId(), x, y, z, h, animation);
	}

	public static void teleportTo(final Player player, final int worldId, final int instanceId, final float x, final float y, final float z,
		final byte heading, TeleportAnimation animation) {
		if (player.isDead()) {
			PlayerReviveService.revive(player, 20, 20, true, 0);
		} else if (DuelService.getInstance().isDueling(player)) {
			DuelService.getInstance().loseDuel(player);
		}
		sendLoc(player, worldId, instanceId, x, y, z, heading, animation);
	}

	public static void showMap(Player player, Npc npc) {
		TeleporterTemplate template = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npc.getNpcId());
		if (template == null)
			log.warn("No teleport id found for " + npc);
		else if (player.isInFlyingState())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_AIRPORT_WHEN_FLYING());
		else if (player.isEnemyFrom(npc))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_WRONG_NPC());
		else
			PacketSendUtility.sendPacket(player, new SM_TELEPORT_MAP(npc.getObjectId(), template.getTeleportId()));
	}

	public static void teleportToPrison(Player player) {
		if (player.getRace() == Race.ELYOS)
			teleportTo(player, WorldMapType.LF_PRISON.getId(), 275, 239, 49);
		else if (player.getRace() == Race.ASMODIANS)
			teleportTo(player, WorldMapType.DF_PRISON.getId(), 275, 239, 49);
	}

	public static void teleportToNpc(Player player, int npcId) {
		SpawnSearchResult searchResult = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(player.getWorldId(), npcId);

		if (searchResult == null) {
			log.warn("No npc spawn found for : " + npcId);
			return;
		}

		SpawnSpotTemplate spot = searchResult.getSpot();
		NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
		float npcRadius = npcTemplate == null ? 1 : npcTemplate.getBoundRadius().getFront(); // StaticObject has no npcTemplate since it's no npc
		WorldMapInstance instance;
		if (player.getWorldId() == searchResult.getWorldId())
			instance = player.getPosition().getWorldMapInstance();
		else if (World.getInstance().getWorldMap(searchResult.getWorldId()).isInstanceType())
			instance = InstanceService.getOrRegisterInstance(searchResult.getWorldId(), player);
		else
			instance = World.getInstance().getWorldMap(searchResult.getWorldId()).getMainWorldMapInstance();

		// calculate position 1m in front of the npc
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(spot.getHeading()));
		float x = spot.getX() + (float) Math.cos(radian) * (1f + npcRadius);
		float y = spot.getY() + (float) Math.sin(radian) * (1f + npcRadius);
		float z = GeoService.getInstance().getZ(searchResult.getWorldId(), x, y, spot.getZ(), instance.getInstanceId());
		if (Float.isNaN(z)) // no collision found or geo disabled
			z = spot.getZ() + 0.5f;
		byte heading = (byte) ((spot.getHeading() & 0xFF) >= 60 ? spot.getHeading() - 60 : spot.getHeading() + 60); // look towards npc

		teleportTo(player, instance, x, y, z, heading, TeleportAnimation.NONE);
	}

	/**
	 * This method will send the set bind point packet
	 */
	public static void sendObeliskBindPoint(Player player) {
		int worldId;
		float x, y, z;
		if (player.getBindPoint() != null) {
			BindPointPosition bplist = player.getBindPoint();
			worldId = bplist.getMapId();
			x = bplist.getX();
			y = bplist.getY();
			z = bplist.getZ();
		} else {
			PlayerInitialData.LocationData locationData = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
			worldId = locationData.getMapId();
			x = locationData.getX();
			y = locationData.getY();
			z = locationData.getZ();
		}
		PacketSendUtility.sendPacket(player, new SM_BIND_POINT_INFO(worldId, x, y, z));
	}

	public static void sendKiskBindPoint(Player player) {
		if (player.getKisk() != null)
			PacketSendUtility.sendPacket(player, new SM_BIND_POINT_INFO(player.getKisk()));
	}

	public static void moveToBindLocation(Player player) {
		float x, y, z;
		int worldId;
		byte h;

		if (player.getBindPoint() != null) {
			BindPointPosition bplist = player.getBindPoint();
			worldId = bplist.getMapId();
			x = bplist.getX();
			y = bplist.getY();
			z = bplist.getZ();
			h = bplist.getHeading();
		} else {
			PlayerInitialData.LocationData locationData = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
			worldId = locationData.getMapId();
			x = locationData.getX();
			y = locationData.getY();
			z = locationData.getZ();
			h = locationData.getHeading();
		}
		teleportTo(player, worldId, x, y, z, h);
	}

	public static void moveToTargetWithDistance(VisibleObject object, Player player, int direction, int distance) {
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(object.getHeading()));
		float x0 = object.getX();
		float y0 = object.getY();
		float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
		teleportTo(player, object.getWorldId(), x0 + x1, y0 + y1, object.getZ());
	}

	public static void moveToInstanceExit(Player player, int worldId, Race race) {
		InstanceExit instanceExit = DataManager.INSTANCE_EXIT_DATA.getInstanceExit(worldId, race);
		if (instanceExit != null && InstanceService.instanceExists(instanceExit.getExitWorld(), 1)) {
			teleportTo(player, instanceExit.getExitWorld(), instanceExit.getX(), instanceExit.getY(), instanceExit.getZ(), instanceExit.getH());
		} else {
			if (instanceExit == null)
				log.warn("No instance exit found for race: " + race + " " + worldId);
			moveToBindLocation(player);
		}
	}

	public static void useTeleportScroll(Player player, String portalName, int worldId) {
		PortalScroll template = DataManager.PORTAL2_DATA.getPortalScroll(portalName);
		if (template == null) {
			log.warn("No portal template found for: " + portalName + " " + worldId);
			return;
		}

		Race playerRace = player.getRace();
		PortalPath portalPath = template.getPortalPath();
		if (portalPath == null) {
			log.warn("No portal scroll for " + playerRace + " on: " + portalName + " " + worldId);
			return;
		}
		PortalLoc loc = DataManager.PORTAL_LOC_DATA.getPortalLoc(portalPath.getLocId());
		if (loc == null) {
			log.warn("No portal loc for locId " + portalPath.getLocId());
			return;
		}
		teleportTo(player, worldId, loc.getX(), loc.getY(), loc.getZ());
	}

	public static void changeChannel(Player player, int channel) {
		World.getInstance().setPosition(player, player.getWorldId(), channel + 1, player.getX(), player.getY(), player.getZ(), player.getHeading());
		player.getController().startProtectionActiveTask();
		PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
		PacketSendUtility.sendPacket(player, new SM_PLAYER_SPAWN(player));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TELEPORT_ZONECHANNEL(channel));
	}

	public static void setEventPos(WorldPosition pos, Race race) {
		if (race == Race.ELYOS) {
			eventPosElyos = new double[] { pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading() };
			log.info("elyos: mapId: " + pos.getMapId() + ", instanceId: " + (int) eventPosElyos[1] + ", X: " + eventPosElyos[2] + ", Y: " + eventPosElyos[3]
				+ ", Z: " + eventPosElyos[4] + ", H: " + (byte) eventPosElyos[5]);
		} else if (race == Race.ASMODIANS) {
			eventPosAsmodians = new double[] { pos.getWorldMapInstance().getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(),
				pos.getHeading() };
			log.info("asmo: mapId: " + pos.getMapId() + ", instanceId: " + (int) eventPosAsmodians[1] + ", X: " + eventPosAsmodians[2] + ", Y: "
				+ eventPosAsmodians[3] + ", Z: " + eventPosAsmodians[4] + ", H: " + (byte) eventPosAsmodians[5]);
		}
	}

	public static void teleportToEvent(Player player) {
		double[] pos = null;
		if (player.getRace() == Race.ELYOS)
			pos = eventPosElyos;
		else if (player.getRace() == Race.ASMODIANS)
			pos = eventPosAsmodians;

		if (pos == null)
			moveToBindLocation(player);
		else
			teleportTo(player, (int) pos[0], (int) pos[1], (float) pos[2], (float) pos[3], (float) pos[4], (byte) pos[5], TeleportAnimation.FADE_OUT_BEAM);
	}

	/**
	 * Sends a teleport request to the player. He will only be teleported to the Npc if he accepts the request.
	 * 
	 * @return True, if the request was sent. False if he already had an active teleport request.
	 */
	public static boolean sendTeleportRequest(Player player, int npcId) {
		int questionMsgId = 905097; // You will be teleported to %0 Continue?
		RequestResponseHandler<Creature> handler = new RequestResponseHandler<>(null) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				teleportToNpc(responder, npcId);
			}
		};

		if (!player.getResponseRequester().putRequest(questionMsgId, handler))
			return false;
		PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(questionMsgId, 0, 0, DataManager.NPC_DATA.getNpcTemplate(npcId).getL10n()));
		return true;
	}

	private static class SpawnTask implements Runnable {

		private final Player player;
		private final int worldId, instanceId;
		private final float x, y, z;
		private final byte h;
		private final TeleportAnimation animation;

		public SpawnTask(Player player, int worldId, int instanceId, float x, float y, float z, byte h, TeleportAnimation animation) {
			this.player = player;
			this.worldId = worldId;
			this.instanceId = instanceId;
			this.x = x;
			this.y = y;
			this.z = z;
			this.h = h;
			this.animation = animation;
		}

		@Override
		public void run() {
			if (player.isSpawned())
				return;

			if (animation != TeleportAnimation.NONE) { // this is a delayed teleport (triggered after animation end)
				if (player.isDead() || !InstanceService.instanceExists(worldId, instanceId)) { // instance might be destroyed after animation end if unlucky
					PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
					World.getInstance().spawn(player);
					return;
				}
				abortPlayerActions(player);
			}

			int currentWorldId = player.getWorldId();
			int currentInstance = player.getInstanceId();
			if (currentWorldId != worldId || currentInstance != instanceId) {
				ConquerorAndProtectorService.getInstance().onLeaveMap(player);
				InstanceService.onLeaveInstance(player);
			}
			World.getInstance().setPosition(player, worldId, instanceId, x, y, z, h);
			World.getInstance().setPosition(player.getPet(), worldId, instanceId, x, y, z, h);

			player.setPortAnimation(animation.getDefaultArrivalAnimation());
			if (currentWorldId == worldId && currentInstance == instanceId) {
				// instant teleport when map is the same
				spawnOnSameMap(player);
			} else {
				// teleport with full map reloading, player will spawn via CM_LEVEL_READY
				PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
				PacketSendUtility.sendPacket(player, new SM_PLAYER_SPAWN(player));
				if (DataManager.WORLD_MAPS_DATA.getTemplate(worldId).isInstance() && !WorldMapType.getWorld(worldId).isPersonal())
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_DUNGEON_OPENED_FOR_SELF(worldId));
			}
			if (player.isLegionMember())
				PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
		}

	}
}
