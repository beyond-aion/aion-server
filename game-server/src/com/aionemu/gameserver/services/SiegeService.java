package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import javolution.util.FastMap;

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
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.OutpostLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.siege.SourceLocation;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
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
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.agentsfight.AgentsFightService;
import com.aionemu.gameserver.services.siegeservice.ArtifactSiege;
import com.aionemu.gameserver.services.siegeservice.FortressSiege;
import com.aionemu.gameserver.services.siegeservice.OutpostSiege;
import com.aionemu.gameserver.services.siegeservice.Siege;
import com.aionemu.gameserver.services.siegeservice.SiegeException;
import com.aionemu.gameserver.services.siegeservice.SiegeStartRunnable;
import com.aionemu.gameserver.services.siegeservice.SourceSiege;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.0 siege update (https://docs.google.com/document/d/1HVOw8-w9AlRp4ci0ei4iAzNaSKzAHj_xORu-qIQJFmc/edit#)
 * 
 * @author SoulKeeper, Source
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
	private static final String BERSERKER_SUNAYAKA_SPAWN_SCHEDULE = SiegeConfig.BERSERKER_SUNAYAKA_SPAWN_SCHEDULE;
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
	/**
	 * Agents Fight schedule
	 */
	private static final String AGENTS_FIGHT_SCHEDULE = SiegeConfig.AGENTS_FIGHT_SCHEDULE;
	
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
	/**
	 * Tiamaranta's eye infiltration route status cl - Western Tiamaranta's Eye Entrance (Center left) cr - Eastern
	 * Tiamaranta's Eye Entrance (Center right) tl - Eye Abyss Gate Elyos (Top left) tr - Eye Abyss Gate Asmodians (Top
	 * rigft)
	 */
	private boolean cl, cr, tl, tr;
	private FastMap<Integer, VisibleObject> tiamarantaPortals = new FastMap<Integer, VisibleObject>();
	private FastMap<Integer, VisibleObject> tiamarantaEyeBoss = new FastMap<Integer, VisibleObject>();
	private FastMap<Integer, VisibleObject> moltenusAbyssBoss = new FastMap<Integer, VisibleObject>();

	// Player list on RVR Event.
	private List<Player> rvrPlayersOnEvent = new ArrayList<Player>();

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
	private Map<Integer, SourceLocation> sources;
	private Map<Integer, SiegeLocation> locations;

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
			sources = DataManager.SIEGE_LOCATION_DATA.getSource();
			locations = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations();
			DAOManager.getDAO(SiegeDAO.class).loadSiegeLocations(locations);
		}
		else {
			artifacts = Collections.emptyMap();
			fortresses = Collections.emptyMap();
			outposts = Collections.emptyMap();
			sources = Collections.emptyMap();
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

		// spawn fortress common npcs
		for (SourceLocation s : getSources().values())
			spawnNpcs(s.getLocationId(), s.getRace(), SiegeModType.PEACE);

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
				CronService.getInstance().schedule(new SiegeStartRunnable(f.getId()), siegeTime);
				log.debug("Scheduled siege of fortressID " + f.getId() + " based on cron expression: " + siegeTime);
			}
		}

		// Schedule sources sieges preparation start
		for (final SiegeSchedule.Source s : siegeSchedule.getSourcesList()) {
			for (String siegeTime : s.getSiegeTimes()) {
				CronService.getInstance().schedule(new SiegeStartRunnable(s.getId()), siegeTime);
				log.debug("Scheduled siege of sourceID " + s.getId() + " based on cron expression: " + siegeTime);
			}
		}

		// Sync Tiamaranta's eye infiltration route status
		updateTiamarantaRiftsStatus(false, true);

		// Tiamatanta Sunayaka start...
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// Only if Elyos and Asmodians each control two Hearts
				Calendar calendar = Calendar.getInstance();
				int bossId = calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY ? 218553 : 219311;
				if (cl && cr) {
					if (tiamarantaEyeBoss.containsKey(bossId) && tiamarantaEyeBoss.get(bossId).isSpawned())
						log.warn("Sunayaka was already spawned...");
					else {
						// TODO: move to datapack
						tiamarantaEyeBoss.put(bossId,
							SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600040000, bossId, 759.0958f, 765.6816f, 1226.5004f, (byte) 0), 1));
						tiamarantaEyeBoss
							.put(283074, SpawnEngine.spawnObject(
								SpawnEngine.addNewSingleTimeSpawn(600040000, 283074, 754.84625f, 819.98828f, 1223.957f, (byte) 0), 1));
						tiamarantaEyeBoss
							.put(283076, SpawnEngine.spawnObject(
								SpawnEngine.addNewSingleTimeSpawn(600040000, 283076, 754.90234f, 712.99042f, 1223.957f, (byte) 0), 1));
					}
					// Berserker is despawned after 1 hr
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							for (VisibleObject vo : tiamarantaEyeBoss.values()) {
								if (vo != null) {
									Npc npc = (Npc) vo;
									if (!npc.getLifeStats().isAlreadyDead()) {
										npc.getController().onDelete();
									}
								}
								tiamarantaEyeBoss.clear();
							}
						}

					}, 3600 * 1000);
				}
			}

		}, BERSERKER_SUNAYAKA_SPAWN_SCHEDULE);

		// Abyss Moltenus start...
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (moltenusAbyssBoss.containsKey(251045) && moltenusAbyssBoss.get(251045).isSpawned())
					log.warn("Moltenus was already spawned...");
				else {
					int randomPos = Rnd.get(1, 3);
					switch (randomPos) {
						case 1:
							moltenusAbyssBoss.put(251045,
								SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2464.9199f, 1689f, 2882.221f, (byte) 0), 1));
							break;
						case 2:
							moltenusAbyssBoss.put(251045, SpawnEngine.spawnObject(
								SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2263.4812f, 2587.1633f, 2879.5447f, (byte) 0), 1));
							break;
						case 3:
							moltenusAbyssBoss.put(251045,
								SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 1692.96f, 1809.04f, 2886.027f, (byte) 0), 1));
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
							for (VisibleObject vo : moltenusAbyssBoss.values()) {
								if (vo != null) {
									Npc npc = (Npc) vo;
									if (!npc.getLifeStats().isAlreadyDead()) {
										npc.getController().onDelete();
									}
								}
								moltenusAbyssBoss.clear();
								log.info("Moltenus dissapeared");
								World.getInstance().doOnAllPlayers(new Visitor<Player>() {

									@Override
									public void visit(Player player) {
										PacketSendUtility.sendBrightYellowMessage(player, "Menotios: Fragment of anger gone");
									}

								});
							}
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
					if (o.isSiegeAllowed()) {
						startSiege(o.getLocationId());
					}
				}
			}

		}, RACE_PROTECTOR_SPAWN_SCHEDULE);

		// Start siege of artifacts
		for (ArtifactLocation artifact : artifacts.values()) {
			if (artifact.isStandAlone()) {
				log.debug("Starting siege of artifact #" + artifact.getLocationId());
				startSiege(artifact.getLocationId());
			}
			else {
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
		
		CronService.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				AgentsFightService.getInstance().onStart();
			}
			
		}, AGENTS_FIGHT_SCHEDULE);
	}

	public void checkSiegeStart(final int locationId) {
		if (getSource(locationId) == null)
			startSiege(locationId);
		// instead of four using one bcoz same start time
		else if (locationId == 4011)
			startPreparations();
	}

	public void startPreparations() {
		log.debug("Starting preparations of all source locations");

		// Set siege start timer..
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// Remove players from Tiamaranta's Eye
				World.getInstance().getWorldMap(600040000).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						TeleportService2.moveToBindLocation(player, true);
					}

				});

				// Start siege warfare
				for (SourceLocation source : getSources().values())
					startSiege(source.getLocationId());
			}

		}, 300 * 1000);

		// 10 sec after start all players moved out and send SM_SHIELD_EFFECT & 2nd SM_SIEGE_LOCATION_STATE
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (SourceLocation source : getSources().values())
					source.clearLocation();

				World.getInstance().getWorldMap(600030000).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						for (SourceLocation source : getSources().values())
							PacketSendUtility.sendPacket(player, new SM_SHIELD_EFFECT(source.getLocationId()));

						for (SourceLocation source : getSources().values())
							PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_STATE(source.getLocationId(), 2));
					}

				});
			}

		}, 310 * 1000);

		for (final SourceLocation source : getSources().values()) {
			source.setPreparation(true);

			if (!source.getRace().equals(SiegeRace.BALAUR)) {
				// Despawn old npc
				deSpawnNpcs(source.getLocationId());

				// Store old owner for msg
				final int oldOwnerRaceId = source.getRace().getRaceId();
				final int legionId = source.getLegionId();
				final String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getLegionName() : "";
				final DescriptionId sourceNameId = new DescriptionId(source.getTemplate().getNameId());

				// Reset owner
				source.setRace(SiegeRace.BALAUR);
				source.setLegionId(0);

				// On start preparations msg
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						if (legionId != 0 && player.getRace().getRaceId() == oldOwnerRaceId)
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301037, legionName, sourceNameId));

						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301039, source.getRace().getDescriptionId(), sourceNameId));

						PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO(source));
					}

				});

				// Spawn new npc
				spawnNpcs(source.getLocationId(), SiegeRace.BALAUR, SiegeModType.PEACE);

				DAOManager.getDAO(SiegeDAO.class).updateSiegeLocation(source);
			}
		}

		// Reset Tiamaranta's eye infiltration route status
		updateTiamarantaRiftsStatus(true, false);
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
				storage = Lists.newArrayList();
				siegeIdToStartTriggers.put(fssr.getLocationId(), storage);
			}
			storage.addAll(CronService.getInstance().getJobTriggers(entry.getValue()));
		}

		// update each fortress next state
		for (Map.Entry<Integer, List<Trigger>> entry : siegeIdToStartTriggers.entrySet()) {
			List<Date> nextFireDates = Lists.newArrayListWithCapacity(entry.getValue().size());
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

			if (fortress instanceof SourceLocation)
				siegeStartHour.add(Calendar.HOUR, 1);

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

	/**
	 * TODO: Check if it's valid
	 * <p/>
	 * If siege duration is endless - will return -1
	 * 
	 * @param siegeLocationId
	 *          Scheduled siege end time
	 * @return remaining seconds in current hour
	 */
	public int getRemainingSiegeTimeInSeconds(int siegeLocationId) {
		Siege<?> siege = getSiege(siegeLocationId);
		if (siege == null || siege.isFinished())
			return 0;

		if (!siege.isStarted())
			return siege.getSiegeLocation().getSiegeDuration();

		// endless siege
		if (siege.getSiegeLocation().getSiegeDuration() == -1)
			return -1;

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, siege.getSiegeLocation().getSiegeDuration());

		int result = (int) ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000);
		return result > 0 ? result : 0;
	}

	public Siege<?> getSiege(SiegeLocation loc) {
		return activeSieges.get(loc.getLocationId());
	}

	public Siege<?> getSiege(Integer siegeLocationId) {
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

	public Map<Integer, SourceLocation> getSources() {
		return sources;
	}

	public SourceLocation getSource(int id) {
		return sources.get(id);
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

	protected Siege<?> newSiege(int siegeLocationId) {
		if (fortresses.containsKey(siegeLocationId))
			return new FortressSiege(fortresses.get(siegeLocationId));
		else if (sources.containsKey(siegeLocationId))
			return new SourceSiege(sources.get(siegeLocationId));
		else if (outposts.containsKey(siegeLocationId))
			return new OutpostSiege(outposts.get(siegeLocationId));
		else if (artifacts.containsKey(siegeLocationId))
			return new ArtifactSiege(artifacts.get(siegeLocationId));
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
			}
			else {
				newFortressRace = outpost.getLocationRace();
			}

			if (SiegeRace.BALAUR == newFortressRace) {
				// In case of balaur fortress ownership
				// oupost also belongs to balaur
				newOutpostRace = SiegeRace.BALAUR;
			}
			else {
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
					if (outpost.isSiegeAllowed()) {
						startSiege(outpost.getLocationId());
					}
					else {
						spawnNpcs(outpost.getLocationId(), outpost.getRace(), SiegeModType.PEACE);
					}
				}
			}
		}
	}

	public void updateTiamarantaRiftsStatus(boolean isPreparation, boolean isSync) {
		int sourceState = 0;
		int aSources = 0;
		int eSources = 0;

		// on prepar no need to chk sources.. all transfer to balaurs
		if (isPreparation)
			broadcastStatusAndUpdate(aSources, eSources, isPreparation, isSync);
		else {
			// Chk owner and current state
			for (SourceLocation source : getSources().values()) {
				sourceState += source.isVulnerable() ? 0 : 1;
				if (source.getRace().equals(SiegeRace.ASMODIANS))
					aSources++;
				else if (source.getRace().equals(SiegeRace.ELYOS))
					eSources++;
			}

			// sourceState(4) - all sieges over or not started
			if (sourceState == 4)
				broadcastStatusAndUpdate(aSources, eSources, isPreparation, isSync);
		}
	}

	private void spawnTiamarantaPortals(boolean cl, boolean cr, boolean tl, boolean tr) {
		SpawnTemplate template;
		// TODO: move to datapack
		if (cl) {
			template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701286, 1524.450f, 1250.425f, 247.048f, (byte) 60);
			template.setStaticId(1594);
			tiamarantaPortals.put(701286, SpawnEngine.spawnObject(template, 1));
		}
		if (cr) {
			template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701287, 1526.465f, 1784.999f, 250.436f, (byte) 60);
			template.setStaticId(2282);
			tiamarantaPortals.put(701287, SpawnEngine.spawnObject(template, 1));
		}
		if (tl) {
			template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701288, 116.665f, 1543.754f, 295.997f, (byte) 0);
			template.setStaticId(681);
			tiamarantaPortals.put(701288, SpawnEngine.spawnObject(template, 1));
		}
		if (tr) {
			template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701289, 117.260f, 1929.155f, 295.691f, (byte) 0);
			template.setStaticId(680);
			tiamarantaPortals.put(701289, SpawnEngine.spawnObject(template, 1));
		}
	}

	private void deSpawnTiamarantaPortals() {
		for (VisibleObject portal : tiamarantaPortals.values()) {
			if (portal != null) {
				portal.getController().onDelete();
			}
			else {
				for (Npc npc : World.getInstance().getNpcs()) {
					if (npc.getNpcId() == 701286 || npc.getNpcId() == 701287 || npc.getNpcId() == 701288 || npc.getNpcId() == 701289) {
						npc.getController().onDelete();
					}
				}
			}
		}
		cl = cr = tl = tr = false;

		// If Sunayaka is still alive we probably must remove him
		for (VisibleObject boss : tiamarantaEyeBoss.values())
			boss.getController().onDelete();

		tiamarantaPortals.clear();
		tiamarantaEyeBoss.clear();
	}

	public void spawnNpcs(int siegeLocationId, SiegeRace race, SiegeModType type) {
		List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(siegeLocationId);
		if (siegeSpawns == null)
			return;
		for (SpawnGroup2 group : siegeSpawns) {
			for (SpawnTemplate template : group.getSpawnTemplates()) {
				SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
				if (siegetemplate.getSiegeRace().equals(race) && siegetemplate.getSiegeModType().equals(type)) {
					Npc npc = (Npc) SpawnEngine.spawnObject(siegetemplate, 1);
					if (SiegeConfig.SIEGE_HEALTH_MOD_ENABLED) {
						NpcTemplate templ = npc.getObjectTemplate();
						if (templ.getRating().equals(NpcRating.LEGENDARY)) {
							NpcLifeStats life = npc.getLifeStats();
							int maxHpPercent = (int) (life.getMaxHp() * SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
							templ.getStatsTemplate().setMaxHp(maxHpPercent);
							life.setCurrentHpPercent(100);
						}
					}
				}
			}
		}
	}

	public void deSpawnNpcs(int siegeLocationId) {
		Collection<SiegeNpc> siegeNpcs = World.getInstance().getLocalSiegeNpcs(siegeLocationId);
		for (SiegeNpc npc : siegeNpcs)
			npc.getController().onDelete();
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

	public void broadcastStatusAndUpdate(int aSources, int eSources, boolean isPreparation, boolean isSync) {
		deSpawnTiamarantaPortals();
		cl = eSources > 1;
		cr = aSources > 1;

		if (isSync)
			spawnTiamarantaPortals(cl, cr, tl = cl, tr = cr);
		else if (!isPreparation && (cl || cr)) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					// Chk is already spawned (if sync or something)
					if (!tl || !tr) {
						spawnTiamarantaPortals(false, false, tl = cl, tr = cr);
						broadcast(new SM_RIFT_ANNOUNCE(cl, cr, tl, tr), null);
					}
				}

			}, 5400000); // 5400000 -> 1h 30m
			spawnTiamarantaPortals(cl, cr, false, false);
		}

		broadcast(new SM_RIFT_ANNOUNCE(cl, cr, tl, tr), null);
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
		if (player.getWorldId() == 600040000) {
			// If player can't get into the eye and sources not in preparation mode.. move to bind
			if (player.getRace() == Race.ELYOS ? !cl : !cr && !getSource(4011).isPreparations()) {
				return false;
			}
			return true;
		}

		for (FortressLocation fortress : getFortresses().values()) {
			if (fortress.isInActiveSiegeZone(player) && fortress.isEnemy(player)) {
				return false;
			}
		}

		for (SourceLocation source : getSources().values()) {
			if (source.isInActiveSiegeZone(player)) {
				WorldPosition pos = source.getEntryPosition();
				World.getInstance().setPosition(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
				return true;
			}
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
			PacketSendUtility.sendPacket(player, new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilenteraAllowed(), getOutpost(2111)
				.isSilenteraAllowed()));
			PacketSendUtility.sendPacket(player, new SM_RIFT_ANNOUNCE(cl, cr, tl, tr));
		}
	}

	public void onEnterSiegeWorld(Player player) {
		// Second part only for siege world
		FastMap<Integer, SiegeLocation> worldLocations = new FastMap<Integer, SiegeLocation>();
		FastMap<Integer, ArtifactLocation> worldArtifacts = new FastMap<Integer, ArtifactLocation>();

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
		 * int siegeLocationId = 10000; Collection<SiegeNpc> siegeNpcs =
		 * World.getInstance().getLocalSiegeNpcs(siegeLocationId); for (SiegeNpc npc : siegeNpcs) { Boolean condition =
		 * npc.getObjectTemplate().getMistSpawnCondition(); if (condition == null) continue; if
		 * (npc.isInsideWeatherZone(entry.getZoneId())) { if (entry.getCode() == 3 && condition || entry.getCode() == 2 &&
		 * !condition) npc.getController().onDelete(); } }
		 */
	}

	public int getFortressId(int locId) {
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
			case 235:
			case 236:
			case 237:
			case 238:
				return 5011; // Sillus Fortress
			case 289:
			case 290:
			case 295:
			case 296:
				return 6011; // Silona Fortress
			case 291:
			case 292:
			case 297:
			case 298:
				return 6021; // Pradeth Fortress
		}
		return 0;
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

	// clear RVR event players list
	public void clearRvrPlayersOnEvent() {
		rvrPlayersOnEvent = new ArrayList<Player>();
	}
}
