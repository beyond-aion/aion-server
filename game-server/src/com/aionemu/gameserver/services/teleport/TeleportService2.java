package com.aionemu.gameserver.services.teleport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
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
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
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
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BIND_POINT_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHANNEL_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_UPDATE_MEMBER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_SPAWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TELEPORT_LOC;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TELEPORT_MAP;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.PrivateStoreService;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author xTz
 * @modified Neon
 */
public class TeleportService2 {

	private static final Logger log = LoggerFactory.getLogger(TeleportService2.class);
	private static double[] eventPosAsmodians;
	private static double[] eventPosElyos;

	/**
	 * Performs flight teleportation
	 *
	 * @param template
	 * @param locId
	 * @param player
	 */
	public static void teleport(TeleporterTemplate template, int locId, Player player, Npc npc, TeleportAnimation animation) {
		TribeClass tribe = npc.getTribe();
		Race race = player.getRace();
		if (tribe.equals(TribeClass.FIELD_OBJECT_LIGHT) && race.equals(Race.ASMODIANS)
			|| (tribe.equals(TribeClass.FIELD_OBJECT_DARK) && race.equals(Race.ELYOS))) {
			return;
		}

		if (template.getTeleLocIdData() == null) {
			log.warn(String.format("Missing locId for this teleporter at teleporter_templates.xml with locId: %d", locId));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			if (player.isGM())
				PacketSendUtility.sendMessage(player, "Missing locId for this teleporter at teleporter_templates.xml with locId: " + locId);
			return;
		}

		TeleportLocation location = template.getTeleLocIdData().getTeleportLocation(locId);
		if (location == null) {
			log.warn(String.format("Missing locId for this teleporter at teleporter_templates.xml with locId: %d", locId));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			if (player.isGM())
				PacketSendUtility.sendMessage(player, "Missing locId for this teleporter at teleporter_templates.xml with locId: " + locId);
			return;
		}

		TelelocationTemplate locationTemplate = DataManager.TELELOCATION_DATA.getTelelocationTemplate(locId);
		if (locationTemplate == null) {
			log.warn(String.format("Missing info at teleport_location.xml with locId: %d", locId));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			if (player.isGM())
				PacketSendUtility.sendMessage(player, "Missing info at teleport_location.xml with locId: " + locId);
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
				FlyPathEntry flypath = DataManager.FLY_PATH.getPathTemplate((short) location.getLocId());
				if (flypath == null) {
					AuditLogger.info(player, "Try to use null flyPath #" + location.getLocId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				double dist = MathUtil.getDistance(player, flypath.getStartX(), flypath.getStartY(), flypath.getStartZ());
				if (dist > 7) {
					AuditLogger.info(player, "Try to use flyPath #" + location.getLocId() + " but hes too far " + dist);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				if (player.getWorldId() != flypath.getStartWorldId()) {
					AuditLogger.info(player, "Try to use flyPath #" + location.getLocId() + " from not native start world " + player.getWorldId()
						+ ". expected " + flypath.getStartWorldId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
					return;
				}

				player.setCurrentFlypath(flypath);
			}
			player.unsetPlayerMode(PlayerMode.RIDE);
			player.setState(CreatureState.FLIGHT_TELEPORT);
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

	/**
	 * Check kinah in inventory for teleportation
	 *
	 * @param location
	 * @param player
	 * @return
	 */
	private static boolean checkKinahForTransportation(TeleportLocation location, Player player) {
		Storage inventory = player.getInventory();

		// TODO: Price vary depending on the influence ratio
		int basePrice = (location.getPrice());
		// TODO check for location.getPricePvp()

		long transportationPrice = PricesService.getPriceForService(basePrice, player.getRace());

		// If HiPassEffect is active, then all flight/teleport prices are 1 kinah
		if (player.getController().isHiPassInEffect())
			transportationPrice = 1;

		if (!inventory.tryDecreaseKinah(transportationPrice, ItemUpdateType.DEC_KINAH_FLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(transportationPrice));
			return false;
		}
		return true;
	}

	private static void sendLoc(Player player, int worldId, int instanceId, float x, float y, float z, byte h, TeleportAnimation animation) {
		abortPlayerActions(player);
		// send teleport animation to player and trigger CM_TELEPORT_DONE when player arrived
		PacketSendUtility.sendPacket(player, new SM_TELEPORT_LOC(worldId, instanceId, x, y, z, h, animation));
		// despawn from world and send animation to others (also ends flying)
		World.getInstance().despawn(player, animation.getDefaultObjectDeleteAnimation());

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (animation.getDuration() > 0 && player.getLifeStats().isAlreadyDead()) {
					World.getInstance().spawn(player);
					PacketSendUtility.broadcastPacket(player, new SM_PLAYER_INFO(player), true);
					return;
				}

				abortPlayerActions(player);
				int currentWorldId = player.getWorldId();
				int currentInstance = player.getInstanceId();
				if (currentWorldId != worldId || currentInstance != instanceId) {
					SerialKillerService.getInstance().onLeaveMap(player);
					InstanceService.onLeaveInstance(player);
				}
				World.getInstance().setPosition(player, worldId, instanceId, x, y, z, h);
				World.getInstance().setPosition(player.getPet(), worldId, instanceId, x, y, z, h);

				player.setPortAnimation(animation.getDefaultArrivalAnimation());
				if (currentWorldId == worldId && currentInstance == instanceId) {
					// instant teleport when map is the same
					spawnOnSameMap(player);
				} else {
					// teleport with full map reloading
					PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
					PacketSendUtility.sendPacket(player, new SM_PLAYER_SPAWN(player));
					if (DataManager.WORLD_MAPS_DATA.getTemplate(worldId).isInstance() && !WorldMapType.getWorld(worldId).isPersonal())
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_DUNGEON_OPENED_FOR_SELF(worldId));
				}
				if (player.isLegionMember())
					PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
			}

		}, animation.getDuration());
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
		if (player.isInFlyState(FlyState.FLYING)) // notify client if we are still flying (client always ends flying after teleport)
			player.getFlyController().startFly(true, true);
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
		} else if (player.getLifeStats().isAlreadyDead()) {
			teleportDeadTo(player, pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		} else {
			teleportTo(player, pos.getMapId(), pos.getInstanceId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), TeleportAnimation.NONE);
		}
	}

	public static void teleportDeadTo(Player player, int worldId, int instanceId, float x, float y, float z, byte heading) {
		if (player.getWorldId() != worldId || player.getInstanceId() != instanceId) {
			SerialKillerService.getInstance().onLeaveMap(player);
			InstanceService.onLeaveInstance(player);
		}
		World.getInstance().setPosition(player, worldId, instanceId, x, y, z, heading);
		PacketSendUtility.sendPacket(player, new SM_CHANNEL_INFO(player.getPosition()));
		PacketSendUtility.sendPacket(player, new SM_PLAYER_SPAWN(player));
		player.setPortAnimation(ArrivalAnimation.LANDING);
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));

		if (player.isLegionMember()) {
			PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
		}
	}

	public static boolean teleportTo(Player player, int worldId, float x, float y, float z) {
		return teleportTo(player, worldId, x, y, z, player.getHeading(), TeleportAnimation.NONE);
	}

	public static boolean teleportTo(Player player, int worldId, float x, float y, float z, byte h) {
		return teleportTo(player, worldId, x, y, z, h, TeleportAnimation.NONE);
	}

	public static boolean teleportTo(Player player, int worldId, float x, float y, float z, byte h, TeleportAnimation animation) {
		return teleportTo(player, worldId, player.getWorldId() != worldId ? 1 : player.getInstanceId(), x, y, z, h, animation);
	}

	public static boolean teleportTo(Player player, int worldId, int instanceId, float x, float y, float z) {
		return teleportTo(player, worldId, instanceId, x, y, z, player.getHeading(), TeleportAnimation.NONE);
	}

	public static boolean teleportTo(Player player, int worldId, int instanceId, float x, float y, float z, byte h) {
		return teleportTo(player, worldId, instanceId, x, y, z, h, TeleportAnimation.NONE);
	}

	public static boolean teleportTo(final Player player, final int worldId, final int instanceId, final float x, final float y, final float z,
		final byte heading, TeleportAnimation animation) {
		if (player.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
			PlayerReviveService.revive(player, 20, 20, true, 0);
		} else if (DuelService.getInstance().isDueling(player.getObjectId())) {
			DuelService.getInstance().loseDuel(player);
		}
		sendLoc(player, worldId, instanceId, x, y, z, heading, animation);
		return true;
	}

	/**
	 * @param player
	 * @param npc
	 */
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

	public static void teleportToCapital(Player player) {
		switch (player.getRace()) {
			case ELYOS:
				TeleportService2.teleportTo(player, WorldMapType.SANCTUM.getId(), 1, 1322, 1511, 568);
				break;
			case ASMODIANS:
				TeleportService2.teleportTo(player, WorldMapType.PANDAEMONIUM.getId(), 1, 1679, 1400, 195);
				break;
		}
	}

	public static void teleportToPrison(Player player) {
		if (player.getRace() == Race.ELYOS)
			teleportTo(player, WorldMapType.DE_PRISON.getId(), 275, 239, 49);
		else if (player.getRace() == Race.ASMODIANS)
			teleportTo(player, WorldMapType.DF_PRISON.getId(), 275, 239, 49);
	}

	public static void teleportToNpc(Player player, int npcId) {
		SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getFirstSpawnByNpcId(player.getWorldId(), npcId);

		if (searchResult == null) {
			log.warn("No npc spawn found for : " + npcId);
			return;
		}

		SpawnSpotTemplate spot = searchResult.getSpot();
		WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(searchResult.getWorldId());
		NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
		int instanceId = player.getInstanceId();
		float x = spot.getX(), y = spot.getY(), z = spot.getZ();
		byte heading = (byte) ((spot.getHeading() & 0xFF) >= 60 ? spot.getHeading() - 60 : spot.getHeading() + 60); // look towards npc

		if (GeoDataConfig.GEO_ENABLE) {
			// calculate position 1m in front of the npc
			double radian = Math.toRadians(MathUtil.convertHeadingToDegree(spot.getHeading()));
			x += Math.cos(radian) * (1f + npcTemplate.getBoundRadius().getFront());
			y += Math.sin(radian) * (1f + npcTemplate.getBoundRadius().getFront());
			z = GeoService.getInstance().getZ(searchResult.getWorldId(), x, y, spot.getZ(), 0.5f, 1);
		}

		if (player.getWorldId() != searchResult.getWorldId()) {
			if (worldTemplate.isInstance()) {
				WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(searchResult.getWorldId());
				InstanceService.registerPlayerWithInstance(newInstance, player);
				instanceId = newInstance.getInstanceId();
			} else
				instanceId = 1;
		}

		teleportTo(player, searchResult.getWorldId(), instanceId, x, y, z, heading);
	}

	/**
	 * This method will send the set bind point packet
	 *
	 * @param player
	 */
	public static void sendSetBindPoint(Player player) {
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
		PacketSendUtility.sendPacket(player, new SM_BIND_POINT_INFO(worldId, x, y, z, player));
	}

	/**
	 * This method will move a player to their bind location
	 *
	 * @param player
	 */
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

	/**
	 * Move Player concerning object with specific conditions
	 *
	 * @param object
	 * @param player
	 * @param direction
	 * @param distance
	 * @return true or false
	 */
	public static boolean moveToTargetWithDistance(VisibleObject object, Player player, int direction, int distance) {
		double radian = Math.toRadians(object.getHeading() * 3);
		float x0 = object.getX();
		float y0 = object.getY();
		float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
		return teleportTo(player, object.getWorldId(), x0 + x1, y0 + y1, object.getZ());
	}

	public static void moveToInstanceExit(Player player, int worldId, Race race) {
		InstanceExit instanceExit = DataManager.INSTANCE_EXIT_DATA.getInstanceExit(worldId, race);
		if (instanceExit != null && InstanceService.isInstanceExist(instanceExit.getExitWorld(), 1)) {
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
			log.warn("No portal loc for locId" + portalPath.getLocId());
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
	 * @param player
	 * @param npcId
	 * @return True, if the request was sent. False if he already had an active teleport request.
	 */
	public static boolean sendTeleportRequest(Player player, int npcId) {
		int questionMsgId = 905097; // You will be teleported to %0 Continue?
		RequestResponseHandler handler = new RequestResponseHandler(null) {

			@Override
			public void denyRequest(Creature requester, Player responder) {
			}

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				teleportToNpc(player, npcId);
			}
		};

		if (!player.getResponseRequester().putRequest(questionMsgId, handler))
			return false;
		PacketSendUtility.sendPacket(player,
			new SM_QUESTION_WINDOW(questionMsgId, 0, 0, new DescriptionId(DataManager.NPC_DATA.getNpcTemplate(npcId).getNameId() * 2 + 1)));
		return true;
	}
}
