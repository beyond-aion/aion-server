package com.aionemu.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.ConsoleUtil;
import com.aionemu.commons.utils.info.SystemInfoUtil;
import com.aionemu.commons.utils.info.VersionInfoUtil;
import com.aionemu.gameserver.ai2.AI2Engine;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.InstanceEngine;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.house.MaintenanceTask;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.network.aion.GameConnectionFactoryImpl;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.AnnouncementService;
import com.aionemu.gameserver.services.AtreianPassportService;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.ChallengeTaskService;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.services.CronJobService;
import com.aionemu.gameserver.services.CuringZoneService;
import com.aionemu.gameserver.services.DatabaseCleaningService;
import com.aionemu.gameserver.services.DebugService;
import com.aionemu.gameserver.services.DisputeLandService;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.FlyRingService;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.LegionDominionService;
import com.aionemu.gameserver.services.LimitedItemTradeService;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.services.PeriodicSaveService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.RoadService;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.services.abyss.AbyssRankUpdateService;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.instance.periodic.PeriodicInstanceManager;
import com.aionemu.gameserver.services.player.PlayerLimitService;
import com.aionemu.gameserver.services.reward.RewardService;
import com.aionemu.gameserver.services.transfers.PlayerTransferService;
import com.aionemu.gameserver.services.webshop.WebshopService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.TemporarySpawnEngine;
import com.aionemu.gameserver.taskmanager.fromdb.TaskFromDBManager;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.ThreadUncaughtExceptionHandler;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.cron.ThreadPoolManagerRunnableRunner;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.utils.javaagent.JavaAgentUtils;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneService;

import ch.lambdaj.Lambda;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import javolution.util.FastTable;

/**
 * <tt>GameServer</tt> is the main class of the application and represents the whole game server.<br>
 * This class is also an entry point with main() method.
 * 
 * @author -Nemesiss-, SoulKeeper, cura
 * @modified Neon
 */
public class GameServer {

	private static final Logger log = LoggerFactory.getLogger(GameServer.class);

	public static final int START_TIME_SECONDS = (int) (ManagementFactory.getRuntimeMXBean().getStartTime() / 1000);

	// TODO remove all this shit
	private static int ELYOS_COUNT = 0;
	private static int ASMOS_COUNT = 0;
	private static float ELYOS_RATIO = 0f;
	private static float ASMOS_RATIO = 0f;
	private static final ReentrantLock lock = new ReentrantLock();

	/**
	 * Prevent instantiation
	 */
	private GameServer() {
	}

	/**
	 * Archives old logs in log folder (recursively) and configures the logging service.
	 */
	private static void initalizeLoggger() {
		try {
			Path logFolder = Paths.get("./log");
			Path oldLogsFolder = Paths.get(logFolder + "/archived");
			List<File> files = new FastTable<>();
			File gsStartTimeFile = new File("./log/[server_start_marker]");
			long gsStartTime;
			long[] gsEndTime = { 0 }; // for mutability within a stream (file walker), we need to use an array here

			Files.createDirectories(gsStartTimeFile.toPath().getParent());
			gsStartTimeFile.createNewFile(); // creates the file only if it does not exists
			gsStartTime = gsStartTimeFile.lastModified();
			gsStartTimeFile.setLastModified(ManagementFactory.getRuntimeMXBean().getStartTime()); // update with new server start time

			Files.createDirectories(logFolder);
			Files.walkFileTree(logFolder, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (!attrs.isDirectory() && file.toString().toLowerCase().endsWith(".log")) {
						files.add(file.toFile());
						if (gsEndTime[0] < attrs.lastModifiedTime().toMillis())
							gsEndTime[0] = attrs.lastModifiedTime().toMillis();
					}
					return FileVisitResult.CONTINUE;
				}
			});

			if (!files.isEmpty()) {
				Files.createDirectories(oldLogsFolder);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm");
				String outFilename = (gsStartTime < gsEndTime[0] ? sdf.format(gsStartTime) : "Unknown") + " to " + sdf.format(gsEndTime[0]) + ".zip";
				try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(oldLogsFolder + "/" + outFilename))) {
					byte[] buf = new byte[1024];
					out.setMethod(ZipOutputStream.DEFLATED);
					out.setLevel(Deflater.BEST_COMPRESSION);
					for (File logFile : files) {
						try (FileInputStream in = new FileInputStream(logFile)) {
							out.putNextEntry(new ZipEntry(logFolder.relativize(logFile.toPath()).toString()));
							int len;
							while ((len = in.read(buf)) > 0)
								out.write(buf, 0, len);
							out.closeEntry();
						}
					}
				}
				for (File logFile : files) { // remove files after successful archiving
					logFile.delete();
					logFile.getParentFile().delete(); // attempt to delete the parent directory (only succeeds if empty)
				}
			}
		} catch (IOException | SecurityException e) {
			throw new RuntimeException("Error gathering and archiving old logs, shutting down...", e);
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			lc.reset();
			configurator.doConfigure("config/slf4j-logback.xml");
		} catch (JoranException je) {
			throw new RuntimeException("Failed to configure loggers, shutting down...", je);
		}
	}

	/**
	 * Launching method for GameServer
	 * 
	 * @param args
	 *          arguments, not used
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		initalizeLoggger();
		initUtilityServicesAndConfig();
		DatabaseCleaningService.getInstance();

		ConsoleUtil.printSection("IDFactory");
		IDFactory.getInstance();

		ConsoleUtil.printSection("Static Data");
		DataManager.getInstance();

		ConsoleUtil.printSection("Handlers");
		loadMultithreaded(QuestEngine.getInstance(), AI2Engine.getInstance(), InstanceEngine.getInstance(), ChatProcessor.getInstance(),
			ZoneService.getInstance()); // ZoneService before GeoService

		ConsoleUtil.printSection("Geodata");
		GeoService.getInstance().initializeGeo();
		// ZoneService.getInstance().saveMaterialZones();
		System.gc();

		ConsoleUtil.printSection("World");
		World.getInstance();
		GameTimeService.getInstance();

		ConsoleUtil.printSection("Drops");
		DropRegistrationService.getInstance();

		// This is loading only siege location data, no siege schedule or spawns
		ConsoleUtil.printSection("Location Data");
		BaseService.getInstance().initBaseLocations();
		SiegeService.getInstance().initSiegeLocations();
		MonsterRaidService.getInstance().initMonsterRaidLocations();
		// DAOManager.getDAO(SiegeMercenariesDAO.class).loadActiveMercenaries();
		VortexService.getInstance().initVortexLocations();
		RiftService.getInstance().initRiftLocations();
		LegionDominionService.getInstance().initLocations();

		ConsoleUtil.printSection("Spawns");
		SpawnEngine.spawnAll();
		RiftService.getInstance().initRifts();
		TemporarySpawnEngine.spawnAll();
		if (SiegeConfig.SIEGE_ENABLED)
			ShieldService.getInstance().spawnAll();
		FlyRingService.getInstance();

		ConsoleUtil.printSection("Limits");
		if (GSConfig.ENABLE_RATIO_LIMITATION) { // TODO move all of this stuff in a separate class / service
			ASMOS_COUNT = DAOManager.getDAO(PlayerDAO.class).getCharacterCountForRace(Race.ASMODIANS);
			ELYOS_COUNT = DAOManager.getDAO(PlayerDAO.class).getCharacterCountForRace(Race.ELYOS);
			updateRatio(null, 0);
		}
		LimitedItemTradeService.getInstance().start();
		if (CustomConfig.LIMITS_ENABLED)
			PlayerLimitService.getInstance().scheduleUpdate();

		// Init Sieges... It's separated due to spawn engine.
		// It should not spawn siege NPCs
		ConsoleUtil.printSection("Siege Schedule");
		SiegeService.getInstance().initSieges();

		ConsoleUtil.printSection("World Bases");
		BaseService.getInstance().initBases();

		ConsoleUtil.printSection("Monster Raid");
		MonsterRaidService.getInstance().initMonsterRaids();

		ConsoleUtil.printSection("Serial Killers");
		SerialKillerService.getInstance().initSerialKillers();

		ConsoleUtil.printSection("Dispute Lands");
		DisputeLandService.getInstance().init();

		ConsoleUtil.printSection("TaskManagers");
		AnnouncementService.getInstance();
		DebugService.getInstance();
		WeatherService.getInstance();
		BrokerService.getInstance();
		Influence.getInstance();
		ExchangeService.getInstance();
		PeriodicSaveService.getInstance();
		AtreianPassportService.getInstance();
		WebshopService.getInstance();
		CronJobService.getInstance();

		InstanceService.load();

		if (!GeoDataConfig.GEO_MATERIALS_ENABLE)
			CuringZoneService.getInstance();
		RoadService.getInstance();
		HTMLCache.getInstance();
		AbyssRankingCache.getInstance();
		AbyssRankUpdateService.getInstance().scheduleUpdate();
		TaskFromDBManager.getInstance();
		ConsoleUtil.printSection("Periodic Instances");
		PeriodicInstanceManager.getInstance();
		if (CustomConfig.ENABLE_REWARD_SERVICE)
			RewardService.getInstance();
		EventService.getInstance();

		ConsoleUtil.printSection("Access Management");
		AdminService.getInstance();
		CommandsAccessService.getInstance();

		ConsoleUtil.printSection("Player Transfers");
		PlayerTransferService.getInstance();

		ConsoleUtil.printSection("Housing");
		HousingBidService.getInstance().start();
		MaintenanceTask.getInstance();
		TownService.getInstance();
		ChallengeTaskService.getInstance();
		GameTimeService.getInstance().startClock();

		System.gc();

		ConsoleUtil.printSection("System Info");
		VersionInfoUtil.printAllInfo(GameServer.class);
		SystemInfoUtil.printAllInfo();

		NioServer nioServer = initNioServer();
		Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());
		log.info("Game Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		
		LoginServer.getInstance().connect(nioServer);
		if (GSConfig.ENABLE_CHAT_SERVER)
			ChatServer.getInstance().connect(nioServer);
	}

	/**
	 * Starts servers for connection with aion client and login\chat server.
	 */
	private static NioServer initNioServer() {
		NioServer nioServer = new NioServer(NetworkConfig.NIO_READ_WRITE_THREADS,
			new ServerCfg(NetworkConfig.CLIENT_SOCKET_ADDRESS, "Aion Connections", new GameConnectionFactoryImpl()));
		nioServer.connect();
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
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

		// make sure that callback code was initialized
		if (JavaAgentUtils.isConfigured())
			log.info("JavaAgent [Callback Support] is configured.");

		// init config
		ConsoleUtil.printSection("Configuration");
		Config.load();
		// Second should be database factory
		ConsoleUtil.printSection("Database");
		DatabaseFactory.init();
		// Initialize DAOs
		DAOManager.init();
		DAOManager.getDAO(PlayerDAO.class).setAllPlayersOffline();
		// Initialize thread pools
		ConsoleUtil.printSection("Threads");
		ThreadPoolManager.getInstance();
		// Initialize cron service
		CronService.initSingleton(ThreadPoolManagerRunnableRunner.class);
	}

	private static void loadMultithreaded(GameEngine... engines) {
		Lambda.enableJitting(true);
		CountDownLatch progressLatch = new CountDownLatch(engines.length);

		for (int i = 0; i < engines.length; i++) {
			final int index = i;
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					engines[index].load(progressLatch);
				}
			});
		}

		try {
			progressLatch.await();
		} catch (InterruptedException e) {
		}
	}

	public static boolean isShuttingDown() {
		return ShutdownHook.isRunning.get();
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
				default:
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
