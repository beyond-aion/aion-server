package com.aionemu.gameserver.services;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.schedule.SiegeSchedules;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.*;
import com.aionemu.gameserver.model.templates.siegelocation.DoorRepairData;
import com.aionemu.gameserver.model.templates.siegelocation.DoorRepairStone;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.siege.*;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldType;

/**
 * 3.0 siege update (https://docs.google.com/document/d/1HVOw8-w9AlRp4ci0ei4iAzNaSKzAHj_xORu-qIQJFmc/edit#)
 *
 * @author SoulKeeper, Source, Neon, Estrayl
 */
public class SiegeService {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");

	/**
	 * We should broadcast fortress status every hour Actually only influence packet must be sent, but that doesn't matter
	 */
	private static final CronExpression SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE;

	static {
		try {
			SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE = new CronExpression("0 0 * ? * *");
		} catch (ParseException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Singleton that is loaded on the class initialization. Guys, we really do not SingletonHolder classes
	 */
	private static final SiegeService instance = new SiegeService();
	/**
	 * Map that holds fortressId to Siege. We can easily know what fortresses is under siege ATM :)
	 */
	private final Map<Integer, Siege<? extends SiegeLocation>> activeSieges = new ConcurrentHashMap<>();

	// Player list on RVR Event.
	private final AtomicBoolean isInitialized = new AtomicBoolean();
	private final Map<Integer, ArtifactLocation> artifacts;
	private final Map<Integer, FortressLocation> fortresses;
	private final Map<Integer, OutpostLocation> outposts;
	private final Map<Integer, SiegeLocation> locations;
	private AgentLocation agent;
	private Date nextStateUpdateTime;
	private Set<Player> rvrEventPlayers = new HashSet<>();

	public static SiegeService getInstance() {
		return instance;
	}

	private SiegeService() {
		if (SiegeConfig.SIEGE_ENABLED) {
			log.info("Initializing sieges...");

			// initialize current siege locations
			artifacts = DataManager.SIEGE_LOCATION_DATA.getArtifacts();
			fortresses = DataManager.SIEGE_LOCATION_DATA.getFortress();
			outposts = DataManager.SIEGE_LOCATION_DATA.getOutpost();
			locations = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations();
			agent = DataManager.SIEGE_LOCATION_DATA.getAgentLoc();
			SiegeDAO.loadSiegeLocations(locations);
		} else {
			artifacts = Collections.emptyMap();
			fortresses = Collections.emptyMap();
			outposts = Collections.emptyMap();
			locations = Collections.emptyMap();
			log.info("Sieges are disabled in config.");
		}
	}

	private void updateNextStateUpdateTime() {
		nextStateUpdateTime = SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE.getTimeAfter(new Date());
	}

	public void initSieges() {
		if (!isInitialized.compareAndSet(false, true) || !SiegeConfig.SIEGE_ENABLED)
			return;

		// despawn all NPCs spawned by spawn engine.
		// Siege spawns should be controlled by siege service
		for (Integer i : getSiegeLocations().keySet()) {
			deSpawnNpcs(i);
		}

		// spawn fortress common npcs
		for (FortressLocation f : getFortresses().values()) {
			spawnNpcs(f.getLocationId(), f.getRace(), SiegeModType.PEACE);
		}

		// spawn outpost protectors...
		for (OutpostLocation o : getOutposts().values()) {
			spawnNpcs(o.getLocationId(), o.getRace(), SiegeModType.PEACE);
		}

		// spawn artifacts
		for (ArtifactLocation a : getStandaloneArtifacts()) {
			spawnNpcs(a.getLocationId(), a.getRace(), SiegeModType.PEACE);
		}

		// initialize siege schedule
		SiegeSchedules siegeSchedules = SiegeSchedules.load();

		// Schedule fortresses sieges protector spawn
		for (SiegeSchedules.Fortress f : siegeSchedules.getFortresses()) {
			for (String siegeTime : f.getSiegeTimes()) {
				String preparationCron = getPreparationCronString(siegeTime);
				CronService.getInstance().schedule(new SiegeStartRunnable(f.getId()), preparationCron);
				log.debug("Scheduled siege of fortressID " + f.getId() + " based on cron expression: " + preparationCron);
			}
		}

		// Schedule agent fights
		for (SiegeSchedules.AgentFight a : siegeSchedules.getAgentFights()) {
			for (String siegeTime : a.getSiegeTimes()) {
				CronService.getInstance().schedule(new SiegeStartRunnable(a.getId()), siegeTime);
				log.debug("Scheduled agent fight based on cron expression: " + siegeTime);
			}
		}

		// Start siege of artifacts
		for (ArtifactLocation artifact : artifacts.values()) {
			if (artifact.isStandAlone()) {
				log.debug("Starting siege of artifact #" + artifact.getLocationId());
				startSiege(artifact.getLocationId());
			} else {
				log.debug("Artifact #" + artifact.getLocationId() + " siege was not started, it belongs to fortress");
			}
		}

		// We should set valid next state for fortress on startup. No need to broadcast state here, no players @ server ATM
		updateNextStateUpdateTime();
		updateFortressNextState();

		// Schedule siege status broadcast (every hour)
		CronService.getInstance().schedule(() -> {
			updateNextStateUpdateTime();
			updateFortressNextState();
			World.getInstance().forEachPlayer(player -> {
				for (FortressLocation fortress : getFortresses().values())
					PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(fortress.getLocationId(), false));

				PacketSendUtility.sendPacket(player, new SM_FORTRESS_STATUS());

				for (FortressLocation fortress : getFortresses().values())
					PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(fortress.getLocationId(), true));
			});
		}, SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE);
		log.debug("Broadcasting Siege Location status based on expression: " + SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE);
	}

	public void checkSiegeStart(final int locationId) {
		if (agent.getLocationId() == locationId)
			startSiege(locationId);
		else
			startPreparations(locationId);
	}

	private void startPreparations(final int locationId) {
		log.debug("Starting preparations of siege Location:" + locationId);
		FortressLocation loc = getFortress(locationId);
		// Set siege start timer..
		ThreadPoolManager.getInstance().schedule(() -> startSiege(locationId), 300 * 1000);
		if (loc.getTemplate().getMaxOccupyCount() > 0 && loc.getOccupiedCount() >= loc.getTemplate().getMaxOccupyCount()
			&& !loc.getRace().equals(SiegeRace.BALAUR)) {
			log.debug("Resetting fortress to balaur control due to exceeded occupy count! locId:" + locationId);
			resetSiegeLocation(loc);
		}
	}

	public synchronized void startSiege(final int siegeLocationId) {
		log.debug("Starting siege of siege location: " + siegeLocationId);

		// Siege should not be started two times
		if (activeSieges.containsKey(siegeLocationId)) {
			log.error("Attempt to start siege twice for siege location: " + siegeLocationId, new Exception());
			return;
		}
		Siege<? extends SiegeLocation> siege = newSiege(siegeLocationId);
		activeSieges.put(siegeLocationId, siege);

		siege.startSiege();

		// certain sieges are endless
		// should end only manually on siege boss death
		if (siege.isEndless())
			return;

		// schedule siege end
		ThreadPoolManager.getInstance().schedule(() -> stopSiege(siegeLocationId), siege.getSiegeLocation().getSiegeDuration() * 1000);
	}

	public synchronized void stopSiege(int siegeLocationId) {
		log.debug("Stopping siege of siege location: " + siegeLocationId);

		Siege<? extends SiegeLocation> siege = activeSieges.remove(siegeLocationId);
		if (siege == null) {
			log.debug("Siege of siege location " + siegeLocationId + " is not in progress, it was captured earlier?");
			return;
		}
		if (siege.isFinished())
			return;
		siege.stopSiege();
	}

	/**
	 * Used to capture fortresses or artifacts without regular siege
	 */
	public synchronized void captureSiege(SiegeRace sr, int legionId, int locId) {
		SiegeLocation loc = getSiegeLocation(locId);
		Siege<?> s = getSiege(locId);
		if (s != null) {
			s.getSiegeCounter().addRaceDamage(sr, s.getBoss().getLifeStats().getMaxHp() + 1);
			s.setBossKilled(true);
			stopSiege(locId);
			loc.setLegionId(legionId);
		} else {
			deSpawnNpcs(locId);
			loc.setVulnerable(false);
			loc.setUnderShield(false);
			loc.setRace(sr);
			loc.setLegionId(legionId);
			spawnNpcs(locId, sr, SiegeModType.PEACE);
			SiegeDAO.updateSiegeLocation(loc);
			switch (locId) {
				case 2011:
				case 2021:
				case 3011:
				case 3021:
					updateOutpostSiegeState((FortressLocation) loc);
					break;
			}
		}
		broadcastUpdate(loc);
	}

	/*
	 * Return location to balaur control
	 */
	private synchronized void resetSiegeLocation(SiegeLocation loc) {
		// Despawn old npc
		deSpawnNpcs(loc.getLocationId());
		loc.clearLocation(); // remove all players
		// Store old owner for msg
		int oldOwnerRaceId = loc.getRace().getRaceId();
		int legionId = loc.getLegionId();
		String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getName() : "";
		String locL10n = loc.getTemplate().getL10n();

		// Reset owner
		loc.setRace(SiegeRace.BALAUR);
		loc.setLegionId(0);

		if (loc instanceof FortressLocation) {
			ArtifactLocation artifact = getFortressArtifact(loc.getLocationId());
			if (artifact != null) {
				artifact.setRace(SiegeRace.BALAUR);
				artifact.setLegionId(0);
			}
		}
		loc.setOccupiedCount(0);

		// On start preparations msg
		World.getInstance().forEachPlayer(player -> {
			if (legionId != 0 && player.getRace().getRaceId() == oldOwnerRaceId)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ABYSS_GUILD_CASTLE_TAKEN(legionName, locL10n));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ABYSS_WIN_CASTLE(loc.getRace().getL10n(), locL10n));
			PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO(loc));
		});

		broadcastUpdate(loc);

		// Spawn new npc
		spawnNpcs(loc.getLocationId(), SiegeRace.BALAUR, SiegeModType.PEACE);

		SiegeDAO.updateSiegeLocation(loc);
	}

	/**
	 * Updates next state for fortresses
	 */
	private void updateFortressNextState() {
		Map<Integer, Date> startDatesByLocId = collectNextSiegeStartDates();
		// update each fortress next state
		for (Map.Entry<Integer, Date> entry : startDatesByLocId.entrySet()) {
			// update fortress state that will be valid within the next hour
			SiegeLocation fortress = getSiegeLocation(entry.getKey());
			if (!entry.getValue().after(nextStateUpdateTime)) // date is > now and <= next update time (this check also accounts for the preparation time)
				fortress.setNextState(SiegeLocation.STATE_VULNERABLE);
			else
				fortress.setNextState(SiegeLocation.STATE_INVULNERABLE);
		}
	}

	private Map<Integer, Date> collectNextSiegeStartDates() {
		Map<SiegeStartRunnable, Date> dates = CronService.getInstance().findNextFireTimes(SiegeStartRunnable.class, true);
		Map<Integer, Date> nextSiegeStartDates = new HashMap<>(dates.size());
		for (Map.Entry<SiegeStartRunnable, Date> entry : dates.entrySet()) {
			nextSiegeStartDates.compute(entry.getKey().getLocationId(), (k, oldDate) -> {
				Date date = entry.getValue();
				if (oldDate == null || oldDate.after(date))
					return date;
				return oldDate;
			});
		}
		return nextSiegeStartDates;
	}

	/**
	 * @return Number of seconds until fortress.getNextState() will be the current state (max. 3600 since states update every hour).
	 */
	public int getSecondsUntilNextFortressState() {
		if (nextStateUpdateTime == null) // null if siege service is deactivated
			return 0;
		return (int) (nextStateUpdateTime.getTime() - System.currentTimeMillis()) / 1000;
	}

	public int getRemainingSiegeTimeInSeconds(int siegeLocationId) {
		Siege<? extends SiegeLocation> siege = getSiege(siegeLocationId);
		if (siege == null || siege.isFinished() || !siege.isStarted())
			return 0;

		long endTime = siege.getStartTime() / 1000 + siege.getSiegeLocation().getSiegeDuration();
		int secondsLeft = (int) (endTime - System.currentTimeMillis() / 1000);

		return secondsLeft > 0 ? secondsLeft : 0;
	}

	public Siege<? extends SiegeLocation> getSiege(SiegeLocation loc) {
		return getSiege(loc.getLocationId());
	}

	public Siege<? extends SiegeLocation> getSiege(int siegeLocationId) {
		return activeSieges.get(siegeLocationId);
	}

	public boolean isSiegeInProgress(int fortressId) {
		return activeSieges.containsKey(fortressId);
	}

	public Map<Integer, OutpostLocation> getOutposts() {
		return outposts;
	}

	public OutpostLocation getOutpost(int id) {
		return outposts.get(id);
	}

	public Map<Integer, FortressLocation> getFortresses() {
		return fortresses;
	}

	public FortressLocation getFortress(int id) {
		return fortresses.get(id);
	}

	public Map<Integer, ArtifactLocation> getArtifacts() {
		return artifacts;
	}

	public ArtifactLocation getArtifact(int id) {
		return getArtifacts().get(id);
	}

	public List<ArtifactLocation> getStandaloneArtifacts() {
		return artifacts.values().stream().filter(ArtifactLocation::isStandAlone).collect(Collectors.toList());
	}

	public ArtifactLocation getFortressArtifact(int siegeLocId) {
		ArtifactLocation loc = getArtifact(siegeLocId);
		return loc == null || loc.getOwningFortress() == null ? null : loc;
	}

	public DoorRepairData getDoorRepairData(int siegeId) {
		FortressLocation fortressLocation = getFortress(siegeId);
		if (fortressLocation == null)
			return null;
		return fortressLocation.getTemplate().getDoorRepairData();
	}

	public DoorRepairStone getRepairStone(int siegeId, int repairStoneStaticId) {
		DoorRepairData doorRepairData = getDoorRepairData(siegeId);
		if (doorRepairData == null)
			return null;
		return doorRepairData.getRepairStone(repairStoneStaticId);
	}

	public Map<Integer, SiegeLocation> getSiegeLocations() {
		return locations;
	}

	public SiegeLocation getSiegeLocation(int id) {
		return locations.get(id);
	}

	public Map<Integer, SiegeLocation> getSiegeLocations(int worldId) {
		Map<Integer, SiegeLocation> mapLocations = new LinkedHashMap<>();
		for (SiegeLocation location : getSiegeLocations().values())
			if (location.getWorldId() == worldId)
				mapLocations.put(location.getLocationId(), location);

		return mapLocations;
	}

	public AgentLocation getAgentLocation() {
		return agent;
	}

	private Siege<? extends SiegeLocation> newSiege(int siegeLocationId) {
		if (fortresses.containsKey(siegeLocationId))
			return new FortressSiege(fortresses.get(siegeLocationId));
		else if (outposts.containsKey(siegeLocationId))
			return new OutpostSiege(outposts.get(siegeLocationId));
		else if (artifacts.containsKey(siegeLocationId))
			return new ArtifactSiege(artifacts.get(siegeLocationId));
		else if (agent != null && agent.getLocationId() == siegeLocationId)
			return new AgentSiege(agent);
		else
			throw new SiegeException("Unknown siege handler for siege location: " + siegeLocationId);
	}

	public void cleanLegionId(int legionId) {
		for (SiegeLocation loc : getSiegeLocations().values()) {
			if (loc.getLegionId() == legionId) {
				loc.setLegionId(0);
				break;
			}
		}
	}

	public synchronized void updateOutpostSiegeState(FortressLocation fortressLoc) {
		for (OutpostLocation outpost : getOutposts().values()) {
			List<Integer> dependencies = outpost.getFortressDependency();
			if (!dependencies.contains(fortressLoc.getLocationId()))
				continue;
			if (dependencies.stream().anyMatch(dependency -> getSiegeLocation(dependency).isVulnerable()))
				break;

			SiegeRace validRaceForSiege = outpost.getLocationId() == 2111 ? SiegeRace.ASMODIANS : SiegeRace.ELYOS;
			boolean isSiegeAllowed = true;

			for (Integer fortressId : dependencies) {
				SiegeRace dependencyFortressRace = getFortresses().get(fortressId).getRace();
				if (validRaceForSiege != dependencyFortressRace) {
					isSiegeAllowed = false;
					break;
				}
			}

			stopSiege(outpost.getLocationId());
			deSpawnNpcs(outpost.getLocationId());

			// broadcast to all new Silentera infiltration route state
			broadcastStatusAndUpdate(outpost, outpost.isSilenteraAllowed());

			// spawn NPC's or sieges
			if (isSiegeAllowed)
				startSiege(outpost.getLocationId());
			else
				spawnNpcs(outpost.getLocationId(), outpost.getRace(), SiegeModType.PEACE);
		}
	}

	public void spawnNpcs(int siegeLocationId, SiegeRace race, SiegeModType type) {
		List<SpawnGroup> siegeSpawns = DataManager.SPAWNS_DATA.getSiegeSpawnsByLocId(siegeLocationId);
		if (siegeSpawns == null)
			return;
		for (SpawnGroup group : siegeSpawns) {
			for (SpawnTemplate template : group.getSpawnTemplates()) {
				SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
				if (siegetemplate.getSiegeRace() == race && siegetemplate.getSiegeModType() == type) {
					SpawnEngine.spawnObject(siegetemplate, 1);
				}
			}
		}
	}

	public void deSpawnNpcs(int siegeLocationId) {
		// iterate over an array copy, since onDelete directly modifies the underlying collection
		for (Object siegeNpc : World.getInstance().getLocalSiegeNpcs(siegeLocationId).toArray())
			((SiegeNpc) siegeNpc).getController().delete();
	}

	public boolean isRespawnAllowed(Npc npc) {
		if (npc instanceof SiegeNpc) {
			FortressLocation fort = getFortress(((SiegeNpc) npc).getSiegeId());
			if (fort != null) {
				if (fort.isVulnerable())
					return false;
				else if (fort.getNextState() == SiegeLocation.STATE_VULNERABLE)
					return npc.getSpawn().getRespawnTime() < getSecondsUntilNextFortressState();
			}
		}
		return true;
	}

	public void broadcastUpdate(SiegeLocation loc) {
		Influence.getInstance().recalculateInfluence();
		SM_SIEGE_LOCATION_INFO pkt1 = new SM_SIEGE_LOCATION_INFO(loc);
		SM_INFLUENCE_RATIO pkt2 = new SM_INFLUENCE_RATIO();
		World.getInstance().forEachPlayer(player -> {
			PacketSendUtility.sendPacket(player, pkt1);
			PacketSendUtility.sendPacket(player, pkt2);
		});
	}

	public void broadcastStatusAndUpdate(OutpostLocation outpost, boolean oldSilentraState) {
		SM_SYSTEM_MESSAGE info = null;
		if (oldSilentraState != outpost.isSilenteraAllowed()) {
			if (outpost.isSilenteraAllowed())
				info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_SPAWN()
					: SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_SPAWN();
			else
				info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_DESPAWN()
					: SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_DESPAWN();
		}

		broadcast(new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilenteraAllowed(), getOutpost(2111).isSilenteraAllowed()), info);
	}

	private void broadcast(final SM_RIFT_ANNOUNCE rift, final SM_SYSTEM_MESSAGE info) {
		World.getInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				PacketSendUtility.sendPacket(player, rift);
				if (info != null && player.getWorldType().equals(WorldType.BALAUREA))
					PacketSendUtility.sendPacket(player, info);
			}

		});
	}

	public FortressLocation findFortress(int worldId, float x, float y, float z) {
		for (FortressLocation fortress : getFortresses().values()) {
			if (fortress.getWorldId() == worldId && fortress.isInsideLocation(x, y, z))
				return fortress;
		}
		return null;
	}

	public void onPlayerLogin(final Player player) {
		// not on login
		// PacketSendUtility.sendPacket(player, new SM_ABYSS_ARTIFACT_INFO(getSiegeLocations().values()));
		// PacketSendUtility.sendPacket(player, new SM_ABYSS_ARTIFACT_INFO2(getSiegeLocations().values()));

		// Chk login when teleporter is dead
		// for (FortressLocation loc : getFortresses().values()) {
		// // remove teleportation to dead teleporters
		// if (!loc.isCanTeleport(player))
		// PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(loc.getLocationId(), false));
		// }

		// First part will be sent to all
		if (SiegeConfig.SIEGE_ENABLED) {
			PacketSendUtility.sendPacket(player, new SM_INFLUENCE_RATIO());
			PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO());
			PacketSendUtility.sendPacket(player, new SM_AFTER_SIEGE_LOCINFO_475());
			PacketSendUtility.sendPacket(player, new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilenteraAllowed(), getOutpost(2111).isSilenteraAllowed()));
		}
	}

	public void onEnterSiegeWorld(Player player) {
		// Second part only for siege world
		Map<Integer, SiegeLocation> worldLocations = new LinkedHashMap<>();
		Map<Integer, ArtifactLocation> worldArtifacts = new LinkedHashMap<>();

		for (SiegeLocation location : getSiegeLocations().values())
			if (location.getWorldId() == player.getWorldId())
				worldLocations.put(location.getLocationId(), location);

		for (ArtifactLocation artifact : getArtifacts().values())
			if (artifact.getWorldId() == player.getWorldId())
				worldArtifacts.put(artifact.getLocationId(), artifact);

		PacketSendUtility.sendPacket(player, new SM_SHIELD_EFFECT(worldLocations.values()));
		PacketSendUtility.sendPacket(player, new SM_ABYSS_ARTIFACT_INFO3(worldArtifacts.values()));
	}

	public void onAbyssPointsAdded(Player player, VisibleObject obj, int abyssPoints) {
		if (obj instanceof Player || obj instanceof SiegeNpc siegeNpc && siegeNpc.getSpawn().getSiegeModType() != SiegeModType.PEACE)
			activeSieges.values().forEach(a -> a.onAbyssPointsAdded(player, abyssPoints));
	}

	public int getSiegeIdByLocId(int locId) {
		switch (locId) {
			case 49:
			case 61:
				return 1011; // Divine Fortress
			case 36:
			case 54:
				return 1131; // Siel's Western Fortress
			case 37:
			case 55:
				return 1132; // Siel's Eastern Fortress
			case 39:
			case 56:
				return 1141; // Sulfur Archipelago
			case 44:
			case 62:
				return 1211; // Roah Fortress
			case 45:
			case 57:
			case 72:
			case 75:
				return 1221; // Krotan Refuge
			case 46:
			case 58:
			case 73:
			case 76:
				return 1231; // Kysis Fortress
			case 47:
			case 59:
			case 74:
			case 77:
				return 1241; // Miren Fortress
			case 48:
			case 60:
				return 1251; // Asteria Fortress
			case 90:
				return 2011; // Temple of Scales
			case 91:
				return 2021; // Altar of Avarice
			case 93:
				return 3011; // Vorgaltem Citadel
			case 94:
				return 3021; // Crimson Temple
			case 322:
			case 323:
			case 358:
			case 359:
				return 7011; // Wealhtheow Fortress
			case 316:
			case 317:
			case 368:
			case 369:
				return 7012; // Hero's Fall Artifact
			case 370:
			case 371:
				return 7013; // Ashen Glade Artifact
			case 372:
			case 373:
				return 7014; // Molten Cliffs Artifact
			default:
				return 0;
		}
	}

	public Set<Player> getRvrEventPlayers() {
		return rvrEventPlayers;
	}

	/**
	 * Checks if the player is in RVR event list, if not the player is added.
	 */
	public void checkRvrEventPlayer(Player player) {
		if (player != null && !rvrEventPlayers.contains(player))
			rvrEventPlayers.add(player);
	}

	public void clearRvrEventPlayers() {
		rvrEventPlayers = new HashSet<>();
	}

	/*
	 * modifies cron string to 5 minutes earlier to allow preparation methods
	 */
	private String getPreparationCronString(String siegeTime) {
		try {
			String[] cronParts = siegeTime.split(" ");
			byte minutes = Byte.parseByte(cronParts[1]);
			byte hours = Byte.parseByte(cronParts[2]);
			minutes -= 5;
			if (minutes < 0) {
				minutes += 60;
				hours -= 1;
				if (hours < 0)
					throw new UnsupportedOperationException("Failed converting cron expression: " + siegeTime + "\nPreparation over midnight not supported.");
			}
			cronParts[1] = String.valueOf(minutes);
			cronParts[2] = String.valueOf(hours);
			return String.join(" ", cronParts);
		} catch (NumberFormatException e) {
			throw new UnsupportedOperationException("Failed converting cron expression: " + siegeTime, e);
		}
	}
}
