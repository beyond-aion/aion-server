package com.aionemu.gameserver;

import java.lang.management.ManagementFactory;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configuration.transformers.PropertyTransformers;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.logging.Logging;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionemu.commons.utils.concurrent.UncaughtExceptionHandler;
import com.aionemu.commons.utils.info.SystemInfo;
import com.aionemu.commons.utils.info.VersionInfo;
import com.aionemu.gameserver.ai.AIEngine;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.main.CleaningConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.instance.InstanceEngine;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.network.aion.GameConnectionFactoryImpl;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.*;
import com.aionemu.gameserver.services.abyss.AbyssRankUpdateService;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.cron.CronExpressionTransformer;
import com.aionemu.gameserver.services.cron.CronService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
import com.aionemu.gameserver.services.player.PlayerLimitService;
import com.aionemu.gameserver.services.transfers.PlayerTransferService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.taskmanager.tasks.housing.AuctionAutoFillTask;
import com.aionemu.gameserver.taskmanager.tasks.housing.AuctionEndTask;
import com.aionemu.gameserver.taskmanager.tasks.housing.MaintenanceTask;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.cron.ThreadPoolManagerRunnableRunner;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.utils.xml.JAXBUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * <tt>GameServer</tt> is the main class of the application and represents the whole game server.<br>
 * This class is also an entry point with main() method.
 * 
 * @author -Nemesiss-, SoulKeeper, cura, Neon
 */
public class GameServer {

	static {
		Logging.init(); // must run before instantiating any logger
	}
	private static final Logger log = LoggerFactory.getLogger(GameServer.class);

	public static final int START_TIME_SECONDS = (int) (ManagementFactory.getRuntimeMXBean().getStartTime() / 1000);
	public static final VersionInfo versionInfo = new VersionInfo(GameServer.class);

	private static NioServer nioServer;

	// TODO remove all this shit
	private static int ELYOS_COUNT = 0;
	private static int ASMOS_COUNT = 0;
	private static float ELYOS_RATIO = 0f;
	private static float ASMOS_RATIO = 0f;
	private static final ReentrantLock lock = new ReentrantLock();

	private GameServer() {
	}

	static void main() {
		JAXBUtil.preLoadContextAsync(StaticData.class); // do this early so DataManager doesn't need to wait as long
		initUtilityServicesAndConfig();

		IDFactory.getInstance();

		DataManager.getInstance();

		Stream.of(QuestEngine.getInstance(), AIEngine.getInstance(), InstanceEngine.getInstance(), ChatProcessor.getInstance(), ZoneService.getInstance(),
			GeoService.getInstance()).parallel().forEach(GameEngine::init);
		// ZoneService.getInstance().saveMaterialZones();

		World.getInstance();
		GameTimeService.getInstance();

		DropRegistrationService.getInstance();

		// This is loading only siege location data, no siege schedule or spawns
		BaseService.getInstance();
		SiegeService.getInstance();
		WorldRaidService.getInstance().initWorldRaidLocations();
		// DAOManager.getDAO(SiegeMercenariesDAO.class).loadActiveMercenaries();
		VortexService.getInstance().initVortexLocations();
		RiftService.getInstance().initRiftLocations();
		LegionDominionService.getInstance().initLocations();

		HousingService.getInstance(); // init housing service before spawns since it gets called on every instance spawn
		HousingBidService.getInstance();
		AuctionEndTask.getInstance();
		AuctionAutoFillTask.getInstance();
		MaintenanceTask.getInstance();
		ChallengeTaskService.getInstance();

		SpawnEngine.spawnAll();
		TownService.getInstance();
		FlyRingService.getInstance();
		RiftService.getInstance().initRifts();

		if (GSConfig.ENABLE_RATIO_LIMITATION) { // TODO move all of this stuff in a separate class / service
			ASMOS_COUNT = PlayerDAO.getCharacterCountForRace(Race.ASMODIANS);
			ELYOS_COUNT = PlayerDAO.getCharacterCountForRace(Race.ELYOS);
			updateRatio(null, 0);
		}
		LimitedItemTradeService.getInstance().start();
		if (CustomConfig.LIMITS_ENABLED)
			PlayerLimitService.getInstance().scheduleUpdate();

		// Init Sieges... It's separated due to spawn engine.
		// It should not spawn siege NPCs
		SiegeService.getInstance().initSieges();

		BaseService.getInstance().initBases();

		WorldRaidService.getInstance().initWorldRaids();

		ConquerorAndProtectorService.getInstance().init();

		AnnouncementService.getInstance();
		DebugService.getInstance();
		WeatherService.getInstance();
		BrokerService.getInstance();
		Influence.getInstance();
		ExchangeService.getInstance();
		PeriodicSaveService.getInstance();
		AtreianPassportService.getInstance();
		CronJobService.getInstance();

		if (!GeoDataConfig.GEO_MATERIALS_ENABLE)
			CuringZoneService.getInstance();
		RoadService.getInstance();
		HTMLCache.getInstance();
		AbyssRankingCache.getInstance();
		AbyssRankUpdateService.scheduleUpdate();
		PeriodicInstanceManager.getInstance();
		EventService.getInstance().start();

		AdminService.getInstance();
		CommandsAccessService.loadAccesses();

		PlayerTransferService.getInstance();

		GameTimeService.getInstance().startClock();

		PvpMapService.getInstance().init();
		CustomInstanceService.getInstance();
		DataManager.waitForValidationToFinishAndShutdownOnFail();

		System.gc();

		VersionInfo.logAll(versionInfo, GSConfig.TIME_ZONE_ID);
		SystemInfo.logAll();

		nioServer = initNioServer();
		Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());
		log.info("Game server started in " + (System.currentTimeMillis() / 1000 - START_TIME_SECONDS) + " seconds.");

		LoginServer.getInstance().connect(nioServer);
		if (GSConfig.ENABLE_CHAT_SERVER)
			ChatServer.getInstance().connect(nioServer);
	}

	/**
	 * Starts servers for connection with aion client and login\chat server.
	 */
	private static NioServer initNioServer() {
		if (NetworkConfig.NIO_READ_WRITE_THREADS > 1 && !NetworkConfig.NIO_READ_WRITE_THREADS_UNSAFE_ALLOW)
			throw new Error("gameserver.network.nio.threads must not exceed 1 (the game server is not thread-safe)");
		NioServer nioServer = new NioServer(NetworkConfig.NIO_READ_WRITE_THREADS,
			new ServerCfg(NetworkConfig.CLIENT_SOCKET_ADDRESS, "Aion game clients", new GameConnectionFactoryImpl()));
		nioServer.connect(ThreadPoolManager.getInstance());
		return nioServer;
	}

	/**
	 * Initialize all helper services, that are not directly related to aion gs, which includes:
	 * <ul>
	 * <li>Database factory</li>
	 * <li>Thread pool</li>
	 * <li>Cron service</li>
	 * </ul>
	 * This method also initializes {@link Config}
	 */
	private static void initUtilityServicesAndConfig() {
		// Set default uncaught exception handler
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

		PropertyTransformers.register(new CronExpressionTransformer());
		Config.load();
		// Second should be database factory
		DatabaseFactory.init();
		PlayerDAO.setAllPlayersOffline();
		if (CleaningConfig.CLEANING_ENABLE)
			DatabaseCleaningService.deletePlayersOnInactiveAccounts();

		// Initialize thread pools
		ThreadPoolManager.getInstance();

		// Initialize cron service
		CronService.initSingleton(ThreadPoolManagerRunnableRunner.class, TimeZone.getTimeZone(GSConfig.TIME_ZONE_ID));
	}

	public static void shutdownNioServer() {
		if (nioServer != null) {
			nioServer.shutdown();
			nioServer = null;
		}
	}

	public static boolean isShutdownScheduled() {
		return ShutdownHook.getInstance().isRunning();
	}

	public static boolean isShuttingDownSoon() {
		return ShutdownHook.getInstance().isRunning() && ShutdownHook.getInstance().getRemainingSeconds() <= 30;
	}

	public static void initShutdown(int exitCode, int delaySeconds) {
		ShutdownHook.getInstance().initShutdown(exitCode, delaySeconds);
	}

	public static void updateRatio(Race race, int i) {
		if (race == null)
			return;
		lock.lock();
		try {
			switch (race) {
				case ASMODIANS:
					ASMOS_COUNT += i;
					break;
				case ELYOS:
					ELYOS_COUNT += i;
					break;
			}

			if ((ASMOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT) && (ELYOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT)) {
				ASMOS_RATIO = ELYOS_RATIO = 50f;
			} else {
				ASMOS_RATIO = ASMOS_COUNT * 100 / (ASMOS_COUNT + ELYOS_COUNT);
				ELYOS_RATIO = ELYOS_COUNT * 100 / (ASMOS_COUNT + ELYOS_COUNT);
			}
		} finally {
			lock.unlock();
		}

		log.info("FACTIONS RATIO UPDATED: E " + String.format("%.1f", ELYOS_RATIO) + " % / A " + String.format("%.1f", ASMOS_RATIO) + " %");
	}

	public static float getRatiosFor(Race race) {
		switch (race) {
			case ASMODIANS:
				return ASMOS_RATIO;
			case ELYOS:
				return ELYOS_RATIO;
			default:
				return 0f;
		}
	}

	public static int getCountFor(Race race) {
		switch (race) {
			case ASMODIANS:
				return ASMOS_COUNT;
			case ELYOS:
				return ELYOS_COUNT;
			default:
				return 0;
		}
	}

}
