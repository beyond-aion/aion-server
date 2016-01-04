package com.aionemu.gameserver.services;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import javolution.util.FastMap;
import javolution.util.FastTable;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.shedule.SiegeSchedule;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.AgentLocation;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.OutpostLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_ARTIFACT_INFO3;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AFTER_SIEGE_LOCINFO_475;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORTRESS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORTRESS_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INFLUENCE_RATIO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIFT_ANNOUNCE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHIELD_EFFECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.siegeservice.AgentSiege;
import com.aionemu.gameserver.services.siegeservice.ArtifactSiege;
import com.aionemu.gameserver.services.siegeservice.FortressSiege;
import com.aionemu.gameserver.services.siegeservice.OutpostSiege;
import com.aionemu.gameserver.services.siegeservice.Siege;
import com.aionemu.gameserver.services.siegeservice.SiegeException;
import com.aionemu.gameserver.services.siegeservice.SiegeStartRunnable;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * 3.0 siege update (https://docs.google.com/document/d/1HVOw8-w9AlRp4ci0ei4iAzNaSKzAHj_xORu-qIQJFmc/edit#)
 * 
 * @author SoulKeeper, Source
 * @modified Neon, Estrayl
 */
public class SiegeService {

	/**
	 * Just a logger
	 */
	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	/**
	 * Balaurea race protector spawn schedule.
	 */
	private static final String RACE_PROTECTOR_SPAWN_SCHEDULE = SiegeConfig.RACE_PROTECTOR_SPAWN_SCHEDULE;
	/**
	 * Balaurea race protector spawn schedule.
	 */
	private static final String MOLTENUS_SPAWN_SCHEDULE = SiegeConfig.MOLTENUS_SPAWN_SCHEDULE;
	/**
	 * We should broadcast fortress status every hour Actually only influence packet must be sent, but that doesn't matter
	 */
	private static final String SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE = "0 0 * ? * *";
	/**
	 * Singleton that is loaded on the class initialization. Guys, we really do not SingletonHolder classes
	 */
	private static final SiegeService instance = new SiegeService();
	/**
	 * Map that holds fortressId to Siege. We can easily know what fortresses is under siege ATM :)
	 */
	private final Map<Integer, Siege<?>> activeSieges = new ConcurrentHashMap<>();
	/**
	 * Object that holds siege schedule.<br>
	 * And maybe other useful information (in future).
	 */
	private SiegeSchedule siegeSchedule;

	private Npc moltenus;

	// Player list on RVR Event.
	private List<Player> rvrPlayersOnEvent = new FastTable<>();

	/**
	 * Returns the single instance of siege service
	 * 
	 * @return siege service instance
	 */
	public static SiegeService getInstance() {
		return instance;
	}

	private Map<Integer, ArtifactLocation> artifacts;
	private Map<Integer, FortressLocation> fortresses;
	private Map<Integer, OutpostLocation> outposts;
	private Map<Integer, SiegeLocation> locations;
	private AgentLocation agent;

	/**
	 * Initializer. Should be called once.
	 */
	public void initSiegeLocations() {
		if (SiegeConfig.SIEGE_ENABLED) {
			log.info("Initializing sieges...");

			if (siegeSchedule != null) {
				log.error("SiegeService should not be initialized two times!");
				return;
			}

			// initialize current siege locations
			artifacts = DataManager.SIEGE_LOCATION_DATA.getArtifacts();
			fortresses = DataManager.SIEGE_LOCATION_DATA.getFortress();
			outposts = DataManager.SIEGE_LOCATION_DATA.getOutpost();
			locations = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations();
			agent = DataManager.SIEGE_LOCATION_DATA.getAgentLoc();
			DAOManager.getDAO(SiegeDAO.class).loadSiegeLocations(locations);
		} else {
			artifacts = Collections.emptyMap();
			fortresses = Collections.emptyMap();
			outposts = Collections.emptyMap();
			locations = Collections.emptyMap();
			log.info("Sieges are disabled in config.");
		}
	}

	public void initSieges() {
		if (!SiegeConfig.SIEGE_ENABLED)
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
			if (SiegeRace.BALAUR != o.getRace() && o.getLocationRace() != o.getRace()) {
				spawnNpcs(o.getLocationId(), o.getRace(), SiegeModType.PEACE);
			}
		}

		// spawn artifacts
		for (ArtifactLocation a : getStandaloneArtifacts().values()) {
			spawnNpcs(a.getLocationId(), a.getRace(), SiegeModType.PEACE);
		}

		// initialize siege schedule
		siegeSchedule = SiegeSchedule.load();

		// Schedule fortresses sieges protector spawn
		for (final SiegeSchedule.Fortress f : siegeSchedule.getFortressesList()) {
			for (String siegeTime : f.getSiegeTimes()) {
				String preperationCron = getPreperationCronString(siegeTime);
				if (preperationCron != null) {
					CronService.getInstance().schedule(new SiegeStartRunnable(f.getId()), preperationCron);
					log.debug("Scheduled siege of fortressID " + f.getId() + " based on cron expression: " + preperationCron);
				}
			}
		}

		// Schedule agent fights
		for (final SiegeSchedule.AgentFight a : siegeSchedule.getAgentFights()) {
			for (String siegeTime : a.getSiegeTimes()) {
				CronService.getInstance().schedule(new SiegeStartRunnable(a.getId()), siegeTime);
				log.debug("Scheduled agentfight based on cron expression: " + siegeTime);
			}
		}

		// Abyss Moltenus start...
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (moltenus != null && moltenus.isSpawned())
					log.warn("Moltenus was already spawned...");
				else {
					int randomPos = Rnd.get(1, 3);
					switch (randomPos) {
						case 1:
							moltenus = (Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2464.9199f, 1689f, 2882.221f, (byte) 0),
								1);
							break;
						case 2:
							moltenus = (Npc) SpawnEngine.spawnObject(
								SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2263.4812f, 2587.1633f, 2879.5447f, (byte) 0), 1);
							break;
						case 3:
							moltenus = (Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 1692.96f, 1809.04f, 2886.027f, (byte) 0),
								1);
							break;
					}
					log.info("Moltenus spawned in the Abyss");
					World.getInstance().doOnAllPlayers(new Visitor<Player>() {

						@Override
						public void visit(Player player) {
							PacketSendUtility.sendBrightYellowMessage(player, "Menotios: Fragment of the anger has been sighted in the Abyss");
						}

					});
					// Moltenus despawned after 1 hr if not killed
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (moltenus != null && !moltenus.getLifeStats().isAlreadyDead()) {
								moltenus.getController().onDelete();
								moltenus = null;
							}
							log.info("Moltenus dissapeared");
							World.getInstance().doOnAllPlayers(new Visitor<Player>() {

								@Override
								public void visit(Player player) {
									PacketSendUtility.sendBrightYellowMessage(player, "Menotios: Fragment of anger gone");
								}

							});
						}
					}, 3600 * 1000);
				}
			}
		}, MOLTENUS_SPAWN_SCHEDULE);

		// Outpost siege start...
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// spawn outpost protectors...
				for (OutpostLocation o : getOutposts().values()) {
					if (o.isSiegeAllowed())
						startSiege(o.getLocationId());
				}
			}

		}, RACE_PROTECTOR_SPAWN_SCHEDULE);

		// Start siege of artifacts
		for (ArtifactLocation artifact : artifacts.values()) {
			if (artifact.isStandAlone()) {
				log.debug("Starting siege of artifact #" + artifact.getLocationId());
				startSiege(artifact.getLocationId());
			} else {
				log.debug("Artifact #" + artifact.getLocationId() + " siege was not started, it belongs to fortress");
			}
		}

		// We should set valid next state for fortress on startup
		// no need to broadcast state here, no players @ server ATM
		updateFortressNextState();

		// Schedule siege status broadcast (every hour)
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				updateFortressNextState();
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						for (FortressLocation fortress : getFortresses().values())
							PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(fortress.getLocationId(), false));

						PacketSendUtility.sendPacket(player, new SM_FORTRESS_STATUS());

						for (FortressLocation fortress : getFortresses().values())
							PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(fortress.getLocationId(), true));
					}

				});
			}

		}, SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE);
		log.debug("Broadcasting Siege Location status based on expression: " + SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE);
	}

	public void checkSiegeStart(final int locationId) {
		if (agent.getLocationId() == locationId)
			startSiege(locationId);
		else
			startPreparations(locationId);
	}

	public void startPreparations(final int locationId) {
		log.debug("Starting preparations of siege Location:" + locationId);
		FortressLocation loc = this.getFortress(locationId);
		// Set siege start timer..
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// Special case for Anoha Fortress artifacts
				if (locationId == 7011) {
					List<ArtifactLocation> artis = FastTable.of(getArtifact(7012), getArtifact(7013), getArtifact(7014));
					for (ArtifactLocation arti : artis) {
						if (!arti.getRace().equals(SiegeRace.BALAUR))
							resetSiegeLocation(arti);
					}
					artis.clear();
				}
				// Start siege warfare
				startSiege(locationId);
			}

		}, 300 * 1000);
		if (loc.getTemplate().getMaxOccupyCount() > 0 && loc.getOccupiedCount() >= loc.getTemplate().getMaxOccupyCount()
			&& !loc.getRace().equals(SiegeRace.BALAUR)) {
			log.debug("Resetting fortress to balaur control due to exceeded occupy count! locId:" + locationId);
			this.resetSiegeLocation(loc);
		}
	}

	public void startSiege(final int siegeLocationId) {
		log.debug("Starting siege of siege location: " + siegeLocationId);

		// Siege should not be started two times. Never.
		Siege<?> siege;
		synchronized (this) {
			if (activeSieges.containsKey(siegeLocationId)) {
				log.error("Attempt to start siege twice for siege location: " + siegeLocationId);
				return;
			}
			siege = newSiege(siegeLocationId);
			activeSieges.put(siegeLocationId, siege);
		}

		siege.startSiege();

		// certain sieges are endless
		// should end only manually on siege boss death
		if (siege.isEndless()) {
			return;
		}

		// schedule siege end
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				stopSiege(siegeLocationId);
			}

		}, siege.getSiegeLocation().getSiegeDuration() * 1000);
	}

	public void stopSiege(int siegeLocationId) {
		log.debug("Stopping siege of siege location: " + siegeLocationId);

		// Just a check here...
		// If fortresses was captured in 99% the siege timer will return here
		// without concurrent race
		if (!isSiegeInProgress(siegeLocationId)) {
			log.debug("Siege of siege location " + siegeLocationId + " is not in progress, it was captured earlier?");
			return;
		}

		// We need synchronization here for that 1% of cases :)
		// It may happen that fortresses siege is stopping in the same time by 2 different threads
		// 1 is for killing the boss
		// 2 is for the schedule
		// it might happen that siege will be stopping by other thread, but in such case siege object will be null
		Siege<?> siege;
		synchronized (this) {
			siege = activeSieges.remove(siegeLocationId);
		}
		if (siege == null || siege.isFinished()) {
			return;
		}

		siege.stopSiege();
	}

	/*
	 * Return location to balaur control
	 */
	protected void resetSiegeLocation(SiegeLocation loc) {
		// Despawn old npc
		deSpawnNpcs(loc.getLocationId());
		loc.clearLocation(); // remove all players
		// Store old owner for msg
		final int oldOwnerRaceId = loc.getRace().getRaceId();
		final int legionId = loc.getLegionId();
		final String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getLegionName() : "";
		final DescriptionId nameId = new DescriptionId(loc.getTemplate().getNameId());

		// Reset owner
		loc.setRace(SiegeRace.BALAUR);
		loc.setLegionId(0);

		if (loc instanceof FortressLocation) {
			ArtifactLocation artifact = getFortressArtifacts().get(loc.getLocationId());
			if (artifact != null) {
				artifact.setRace(SiegeRace.BALAUR);
				artifact.setLegionId(0);
			}
		}

		if (legionId != 0 && loc.hasValidGpRewards()) { // make sure holding GP are deducted on Capture
			int oldLegionGeneral = LegionService.getInstance().getLegionBGeneral(legionId);
			if (oldLegionGeneral != 0) {
				GloryPointsService.decreaseGp(oldLegionGeneral, 1000);
				LegionService.getInstance().getLegion(legionId).decreaseSiegeGloryPoints(1000);
			}
		}
		loc.setOccupiedCount(0);

		// On start preparations msg
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (legionId != 0 && player.getRace().getRaceId() == oldOwnerRaceId)
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301037, legionName, nameId));

				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301039, loc.getRace().getDescriptionId(), nameId));

				PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO(loc));
			}

		});

		// Spawn new npc
		spawnNpcs(loc.getLocationId(), SiegeRace.BALAUR, SiegeModType.PEACE);

		DAOManager.getDAO(SiegeDAO.class).updateSiegeLocation(loc);
	}

	/**
	 * Updates next state for fortresses
	 */
	protected void updateFortressNextState() {
		// get current hour and add 1 hour
		Calendar currentHourPlus1 = Calendar.getInstance();
		currentHourPlus1.set(Calendar.MINUTE, 0);
		currentHourPlus1.set(Calendar.SECOND, 0);
		currentHourPlus1.set(Calendar.MILLISECOND, 0);
		currentHourPlus1.add(Calendar.HOUR, 1);

		// filter fortress siege start runnables
		Map<Runnable, JobDetail> siegeStartRunables = CronService.getInstance().getRunnables();
		siegeStartRunables = Maps.filterKeys(siegeStartRunables, new Predicate<Runnable>() {

			@Override
			public boolean apply(@Nullable Runnable runnable) {
				return runnable instanceof SiegeStartRunnable;
			}

		});

		// Create map FortressId-To-AllTriggers
		Map<Integer, List<Trigger>> siegeIdToStartTriggers = Maps.newHashMap();
		for (Map.Entry<Runnable, JobDetail> entry : siegeStartRunables.entrySet()) {
			SiegeStartRunnable fssr = (SiegeStartRunnable) entry.getKey();

			List<Trigger> storage = siegeIdToStartTriggers.get(fssr.getLocationId());
			if (storage == null) {
				storage = new FastTable<>();
				siegeIdToStartTriggers.put(fssr.getLocationId(), storage);
			}
			storage.addAll(CronService.getInstance().getJobTriggers(entry.getValue()));
		}

		// update each fortress next state
		for (Map.Entry<Integer, List<Trigger>> entry : siegeIdToStartTriggers.entrySet()) {
			List<Date> nextFireDates = new FastTable<>();
			for (Trigger trigger : entry.getValue()) {
				nextFireDates.add(trigger.getNextFireTime());
			}
			Collections.sort(nextFireDates);

			// clear non-required times
			Date nextSiegeDate = nextFireDates.get(0);
			Calendar siegeStartHour = Calendar.getInstance();
			siegeStartHour.setTime(nextSiegeDate);
			siegeStartHour.set(Calendar.MINUTE, 0);
			siegeStartHour.set(Calendar.SECOND, 0);
			siegeStartHour.set(Calendar.MILLISECOND, 0);

			// update fortress state that will be valid in 1 h
			SiegeLocation fortress = getSiegeLocation(entry.getKey());
			// check if siege duration is > than 1 Hour
			Calendar siegeCalendar = Calendar.getInstance();
			siegeCalendar.set(Calendar.MINUTE, 0);
			siegeCalendar.set(Calendar.SECOND, 0);
			siegeCalendar.set(Calendar.MILLISECOND, 0);
			siegeCalendar.add(Calendar.HOUR, 0);
			siegeCalendar.add(Calendar.SECOND, getRemainingSiegeTimeInSeconds(fortress.getLocationId()));

			if (currentHourPlus1.getTimeInMillis() == siegeStartHour.getTimeInMillis()
				|| siegeCalendar.getTimeInMillis() > currentHourPlus1.getTimeInMillis())
				fortress.setNextState(SiegeLocation.STATE_VULNERABLE);
			else
				fortress.setNextState(SiegeLocation.STATE_INVULNERABLE);
		}
	}

	/**
	 * @return seconds before hour end
	 */
	public int getSecondsBeforeHourEnd() {
		Calendar c = Calendar.getInstance();
		int minutesAsSeconds = c.get(Calendar.MINUTE) * 60;
		int seconds = c.get(Calendar.SECOND);
		return 3600 - (minutesAsSeconds + seconds);
	}

	public int getRemainingSiegeTimeInSeconds(int siegeLocationId) {
		Siege<? extends SiegeLocation> siege = getSiege(siegeLocationId);
		if (siege == null || siege.isFinished() || !siege.isStarted())
			return 0;

		long endTime = siege.getStartTime().toInstant().plusSeconds(siege.getSiegeLocation().getSiegeDuration()).getEpochSecond();
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

	public Map<Integer, ArtifactLocation> getStandaloneArtifacts() {
		return Maps.filterValues(artifacts, new Predicate<ArtifactLocation>() {

			@Override
			public boolean apply(@Nullable ArtifactLocation input) {
				return input != null && input.isStandAlone();
			}

		});
	}

	public Map<Integer, ArtifactLocation> getFortressArtifacts() {
		return Maps.filterValues(artifacts, new Predicate<ArtifactLocation>() {

			@Override
			public boolean apply(@Nullable ArtifactLocation input) {
				return input != null && input.getOwningFortress() != null;
			}

		});
	}

	public Map<Integer, SiegeLocation> getSiegeLocations() {
		return locations;
	}

	public SiegeLocation getSiegeLocation(int id) {
		return locations.get(id);
	}

	public Map<Integer, SiegeLocation> getSiegeLocations(int worldId) {
		Map<Integer, SiegeLocation> mapLocations = new FastMap<Integer, SiegeLocation>();
		for (SiegeLocation location : getSiegeLocations().values())
			if (location.getWorldId() == worldId)
				mapLocations.put(location.getLocationId(), location);

		return mapLocations;
	}

	public AgentLocation getAgentLocation() {
		return agent;
	}

	protected Siege<?> newSiege(int siegeLocationId) {
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
		for (SiegeLocation loc : this.getSiegeLocations().values()) {
			if (loc.getLegionId() == legionId) {
				loc.setLegionId(0);
				break;
			}
		}
	}

	public void updateOutpostStatusByFortress(FortressLocation fortress) {
		for (OutpostLocation outpost : getOutposts().values()) {

			if (!outpost.getFortressDependency().contains(fortress.getLocationId())) {
				continue;
			}

			SiegeRace newFortressRace, newOutpostRace;

			if (!outpost.isRouteSpawned()) {
				// Check if all fortresses are captured by the same owner
				// If not - common fortress race is balaur
				newFortressRace = fortress.getRace();
				for (Integer fortressId : outpost.getFortressDependency()) {
					SiegeRace sr = getFortresses().get(fortressId).getRace();
					if (newFortressRace != sr) {
						newFortressRace = SiegeRace.BALAUR;
						break;
					}
				}
			} else {
				newFortressRace = outpost.getLocationRace();
			}

			if (SiegeRace.BALAUR == newFortressRace) {
				// In case of balaur fortress ownership
				// oupost also belongs to balaur
				newOutpostRace = SiegeRace.BALAUR;
			} else {
				// if fortress owner is non-balaur
				// then outpost owner is opposite to fortress owner
				// Example: if fortresses are captured by Elyos, then outpost should be captured by Asmo
				newOutpostRace = newFortressRace == SiegeRace.ELYOS ? SiegeRace.ASMODIANS : SiegeRace.ELYOS;
			}

			// update outpost race status
			if (outpost.getRace() != newOutpostRace) {
				stopSiege(outpost.getLocationId());
				deSpawnNpcs(outpost.getLocationId());

				// update outpost race and store in db
				outpost.setRace(newOutpostRace);
				DAOManager.getDAO(SiegeDAO.class).updateSiegeLocation(outpost);

				// broadcast to all new Silentera infiltration route state
				broadcastStatusAndUpdate(outpost, outpost.isSilenteraAllowed());

				// spawn NPC's or sieges
				if (SiegeRace.BALAUR != outpost.getRace()) {
					/*
					 * if (outpost.isSiegeAllowed()) { startSiege(outpost.getLocationId()); } else {
					 */
					spawnNpcs(outpost.getLocationId(), outpost.getRace(), SiegeModType.PEACE);
					// }
				}
			}
		}
	}

	public void spawnNpcs(int siegeLocationId, SiegeRace race, SiegeModType type) {
		List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(siegeLocationId);
		if (siegeSpawns == null)
			return;
		for (SpawnGroup2 group : siegeSpawns) {
			for (SpawnTemplate template : group.getSpawnTemplates()) {
				SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
				if (siegetemplate.getSiegeRace().equals(race) && siegetemplate.getSiegeModType().equals(type)) {
					SpawnEngine.spawnObject(siegetemplate, 1);
				}
			}
		}
	}

	public void deSpawnNpcs(int siegeLocationId) {
		// iterate over an array copy, since onDelete directly modifies the underlying collection
		for (Object siegeNpc : World.getInstance().getLocalSiegeNpcs(siegeLocationId).toArray())
			((SiegeNpc) siegeNpc).getController().onDelete();
	}

	public boolean isSiegeNpcInActiveSiege(Npc npc) {
		if (npc instanceof SiegeNpc) {
			FortressLocation fort = getFortress(((SiegeNpc) npc).getSiegeId());
			if (fort != null) {
				if (fort.isVulnerable())
					return true;
				else if (fort.getNextState() == 1)
					return npc.getSpawn().getRespawnTime() >= getSecondsBeforeHourEnd();
			}
		}
		return false;
	}

	public void broadcastUpdate() {
		broadcast(new SM_SIEGE_LOCATION_INFO(), null);
	}

	public void broadcastUpdate(SiegeLocation loc) {
		Influence.getInstance().recalculateInfluence();
		broadcast(new SM_SIEGE_LOCATION_INFO(loc), new SM_INFLUENCE_RATIO());
	}

	public void broadcast(final AionServerPacket pkt1, final AionServerPacket pkt2) {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (pkt1 != null)
					PacketSendUtility.sendPacket(player, pkt1);
				if (pkt2 != null)
					PacketSendUtility.sendPacket(player, pkt2);
			}

		});
	}

	public void broadcastUpdate(SiegeLocation loc, DescriptionId nameId) {
		SM_SIEGE_LOCATION_INFO pkt = new SM_SIEGE_LOCATION_INFO(loc);
		SM_SYSTEM_MESSAGE info = loc.getLegionId() == 0 ? new SM_SYSTEM_MESSAGE(1301039, loc.getRace().getDescriptionId(), nameId)
			: new SM_SYSTEM_MESSAGE(1301038, LegionService.getInstance().getLegion(loc.getLegionId()).getLegionName(), nameId);
		broadcast(pkt, info, loc.getRace());
	}

	private void broadcast(final AionServerPacket pkt, final AionServerPacket info, final SiegeRace race) {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (player.getRace().getRaceId() == race.getRaceId())
					PacketSendUtility.sendPacket(player, info);
				PacketSendUtility.sendPacket(player, pkt);
			}

		});
	}

	public void broadcastStatusAndUpdate(OutpostLocation outpost, boolean oldSilentraState) {
		SM_SYSTEM_MESSAGE info = null;
		if (oldSilentraState != outpost.isSilenteraAllowed()) {
			if (outpost.isSilenteraAllowed())
				info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_SPAWN
					: SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_SPAWN;
			else
				info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_DESPAWN
					: SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_DESPAWN;
		}

		broadcast(new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilenteraAllowed(), getOutpost(2111).isSilenteraAllowed()), info);
	}

	private void broadcast(final SM_RIFT_ANNOUNCE rift, final SM_SYSTEM_MESSAGE info) {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, rift);
				if (info != null && player.getWorldType().equals(WorldType.BALAUREA))
					PacketSendUtility.sendPacket(player, info);
			}

		});
	}

	public boolean validateLoginZone(Player player) {
		for (FortressLocation fortress : getFortresses().values()) {
			if (fortress.isInActiveSiegeZone(player) && fortress.isEnemy(player))
				return false;
		}
		return true;
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
		Map<Integer, SiegeLocation> worldLocations = new FastMap<>();
		Map<Integer, ArtifactLocation> worldArtifacts = new FastMap<>();

		for (SiegeLocation location : getSiegeLocations().values())
			if (location.getWorldId() == player.getWorldId())
				worldLocations.put(location.getLocationId(), location);

		for (ArtifactLocation artifact : getArtifacts().values())
			if (artifact.getWorldId() == player.getWorldId())
				worldArtifacts.put(artifact.getLocationId(), artifact);

		PacketSendUtility.sendPacket(player, new SM_SHIELD_EFFECT(worldLocations.values()));
		PacketSendUtility.sendPacket(player, new SM_ABYSS_ARTIFACT_INFO3(worldArtifacts.values()));
	}

	public void onWeatherChanged(WeatherEntry entry) {
		/*
		 * int siegeLocationId = 10000; Collection<SiegeNpc> siegeNpcs = World.getInstance().getLocalSiegeNpcs(siegeLocationId); for (SiegeNpc npc :
		 * siegeNpcs) { Boolean condition = npc.getObjectTemplate().getMistSpawnCondition(); if (condition == null) continue; if
		 * (npc.isInsideWeatherZone(entry.getZoneId())) { if (entry.getCode() == 3 && condition || entry.getCode() == 2 && !condition)
		 * npc.getController().onDelete(); } }
		 */
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

	// return RVR Event players list
	public List<Player> getRvrPlayersOnEvent() {
		return rvrPlayersOnEvent;
	}

	// check if player is in RVR event list, if not the player is added.
	public void checkRvrPlayerOnEvent(Player player) {
		if (player != null && !rvrPlayersOnEvent.contains(player))
			rvrPlayersOnEvent.add(player);
	}

	/*
	 * modifies cron string to 5 minutes earlier to allow preperation methods
	 */
	private String getPreperationCronString(String siegeTime) {
		String prepCron = "";
		try {
			String[] crons = siegeTime.split(" ");
			byte minutes = Byte.parseByte(crons[1]);
			byte hours = Byte.parseByte(crons[2]);
			minutes -= 5;
			if (minutes < 0) {
				minutes += 60;
				hours -= 1;
				if (hours < 0) {
					log.error("Failed converting cron expression:" + siegeTime + "\npreperation over midnight not supported.");
					return siegeTime;
				}
			}
			crons[1] = String.valueOf(minutes);
			crons[2] = String.valueOf(hours);
			for (int i = 0; i < crons.length; i++) {
				prepCron += crons[i];
				prepCron += " ";
			}
			prepCron = prepCron.trim();

		} catch (NumberFormatException e) {
			log.error("Failed converting cron expression:" + siegeTime, e);
			return null;
		}
		return prepCron;
	}

	// clear RVR event players list
	public void clearRvrPlayersOnEvent() {
		rvrPlayersOnEvent = new FastTable<Player>();
	}
}
