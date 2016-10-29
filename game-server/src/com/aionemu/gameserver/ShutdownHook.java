package com.aionemu.gameserver;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager.SortBy;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.PeriodicSaveService;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author lord_rex
 * @modified Neon
 */
public class ShutdownHook extends Thread {

	private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

	static final AtomicBoolean isRunning = new AtomicBoolean();

	public static ShutdownHook getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public void run() {
		if (ShutdownConfig.HOOK_MODE == 1) {
			shutdown(ShutdownConfig.HOOK_DELAY, ShutdownConfig.ANNOUNCE_INTERVAL, ShutdownMode.SHUTDOWN);
		} else if (ShutdownConfig.HOOK_MODE == 2) {
			shutdown(ShutdownConfig.HOOK_DELAY, ShutdownConfig.ANNOUNCE_INTERVAL, ShutdownMode.RESTART);
		}
	}

	public static enum ShutdownMode {
		NONE("terminating"),
		SHUTDOWN("shutting down"),
		RESTART("restarting");

		private final String text;

		private ShutdownMode(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public void shutdown(int duration, int interval, ShutdownMode mode) {
		// set shutdown status, return if already running
		if (!isRunning.compareAndSet(false, true))
			return;

		for (int i = duration; i >= interval; i -= interval) {
			try {
				if (World.getInstance().getAllPlayers().isEmpty())
					break; // fast exit

				log.info("Runtime is " + mode.getText() + " in " + i + " seconds.");
				PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_SERVER_SHUTDOWN(i));

				sleep(Math.min(interval, i) * 1000);
			} catch (InterruptedException e) {
				// ignore
			} catch (Exception e) {
				log.error("", e);
			}
		}

		ChatServer.getInstance().disconnect();
		LoginServer.getInstance().disconnect();

		// Save all players.
		for (Player activePlayer : World.getInstance().getAllPlayers()) {
			try {
				PlayerLeaveWorldService.leaveWorld(activePlayer);
			} catch (Exception e) {
				log.error("Error while saving player", e);
			}
		}
		log.info("Finished saving players");

		RunnableStatsManager.dumpClassStats(SortBy.AVG);
		PeriodicSaveService.getInstance().onShutdown();

		// Save game time.
		GameTimeService.getInstance().saveGameTime();
		// Shutdown of cron service
		CronService.getInstance().shutdown();
		// ThreadPoolManager shutdown
		ThreadPoolManager.getInstance().shutdown();

		log.info("Runtime is " + mode.getText() + " now...");
		// shut down logger factory to flush all pending log messages
		((LoggerContext) LoggerFactory.getILoggerFactory()).stop();

		// Do system exit.
		Runtime.getRuntime().halt(mode == ShutdownMode.RESTART ? ExitCode.CODE_RESTART : ExitCode.CODE_NORMAL);
	}

	private static final class SingletonHolder {

		private static final ShutdownHook INSTANCE = new ShutdownHook();
	}
}
