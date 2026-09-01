package com.aionemu.gameserver;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager.SortBy;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.PeriodicSaveService;
import com.aionemu.gameserver.services.cron.CronService;
import com.aionemu.gameserver.services.cron.CurrentThreadRunnableRunner;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author lord_rex, Neon
 */
public class ShutdownHook extends Thread {

	private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);
	private static final int UNSET_DELAY = Integer.MIN_VALUE;
	private static final long PENDING_LEAVES_TIMEOUT_MILLIS = 5000;
	private static final long FINAL_PACKET_FLUSH_MILLIS = 1000;
	private final AtomicInteger remainingSeconds = new AtomicInteger(UNSET_DELAY);

	public static ShutdownHook getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private ShutdownHook() {
		if (ShutdownConfig.RESTART_SCHEDULE != null) {
			CronService.getInstance().schedule(() -> System.exit(ExitCode.RESTART), CurrentThreadRunnableRunner.class, ShutdownConfig.RESTART_SCHEDULE,
				true); // CurrentThreadRunnableRunner, otherwise ThreadPoolManager.getInstance().shutdown() will try to wait for this cron task
			log.info("Scheduled automatic server restart based on cron expression: {}", ShutdownConfig.RESTART_SCHEDULE);
		}
	}

	@Override
	public void run() {
		// this method is run when System.exit is triggered, or via other external events like console CTRL+C
		remainingSeconds.compareAndSet(UNSET_DELAY, ShutdownConfig.DELAY);
		for (int announceInterval = 1, expectedSeconds = remainingSeconds.get(); remainingSeconds.get() > 0;) {
			try {
				//An empty world does not mean everyone is gone: players vanish from it in the middle of the leave world procedure.
				if (World.getInstance().getAllPlayers().isEmpty() && !PlayerLeaveWorldService.hasPendingLeaves()) {
					break; //Fast exit.
				}

				if (remainingSeconds.get() % announceInterval == 0) {
					log.info("Runtime is shutting down in " + remainingSeconds + " seconds.");
					PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_SERVER_SHUTDOWN(remainingSeconds.get()));
					announceInterval = nextInterval(remainingSeconds.get(), 5, 60);
				}

				sleep(1000);

				// if remainingSeconds got updated from another thread
				if (!remainingSeconds.compareAndSet(expectedSeconds, --expectedSeconds)) {
					expectedSeconds = remainingSeconds.get();
					announceInterval = 1;
				}
			} catch (InterruptedException ignored) {
			} catch (Exception e) {
				log.error("", e);
			}
		}

		remainingSeconds.set(0);
		awaitPendingLeaves();

		GameServer.shutdownNioServer(); // shuts down network, disconnects cs/ls/all players and saves them
		saveRemainingPlayers();

		RunnableStatsManager.dumpClassStats(SortBy.AVG);
		PeriodicSaveService.getInstance().onShutdown();

		GameTimeService.getInstance().saveGameTime();
		CronService.getInstance().shutdown();
		ThreadPoolManager.getInstance().shutdown();

		// shut down logger factory to flush all pending log messages
		((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
	}

	//Awaits leave world procedures which already started, so clients receive their quit response instead of a broken connection.
	private void awaitPendingLeaves() {
		long timeoutAt = System.currentTimeMillis() + PENDING_LEAVES_TIMEOUT_MILLIS;
		while (PlayerLeaveWorldService.hasPendingLeaves() && System.currentTimeMillis() < timeoutAt) {
			try {
				sleep(100);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		if (PlayerLeaveWorldService.hasPendingLeaves())
			log.warn("Some players are still leaving the world, shutting down anyway.");
		try {
			sleep(FINAL_PACKET_FLUSH_MILLIS); //The quit response is sent after the procedure, so let it leave the send queue.
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	//Players without a connection (e.g. with a pending delayed leave after a crash) are not affected by closing the sockets, so they are saved here.
	private void saveRemainingPlayers() {
		World.getInstance().forEachPlayer(player -> {
			try {
				log.warn("{} was still in the world during shutdown, saving him now.", player);
				PlayerLeaveWorldService.leaveWorld(player);
			} catch (Exception e) {
				log.error("Error saving {} during shutdown.", player, e);
			}
		});
	}

	protected void initShutdown(int exitCode, int delaySeconds) {
		if (delaySeconds < 0)
			return;
		// update shutdown delay if possible (unset or more than one second left)
		int previousValue = remainingSeconds.getAndUpdate(seconds -> seconds == UNSET_DELAY || seconds > 1 ? delaySeconds : seconds);
		if (previousValue == UNSET_DELAY)
			Thread.startVirtualThread(() -> System.exit(exitCode)); // async since System.exit indefinitely blocks the calling thread
	}

	/**
	 * @param remainingSeconds
	 *          - remaining time in seconds, until the shutdown will be performed
	 * @param minInterval
	 *          - minimum interval to be returned (minInterval will equal remainingSeconds if remainingSeconds is shorter)
	 * @param maxInterval
	 *          - maximum interval to be returned
	 * @return The interval (in seconds) to wait until the next announce should be sent to all players.
	 */
	private static int nextInterval(int remainingSeconds, int minInterval, int maxInterval) {
		if (remainingSeconds < minInterval)
			minInterval = Math.max(1, remainingSeconds);
		int interval = remainingSeconds / 2;
		interval = interval / 5 * 5; // ensure a "clean" interval (dividable by 5, like 5, 10, 15s and so on)
		return Math.min(maxInterval, Math.max(minInterval, interval));
	}

	protected boolean isRunning() {
		return remainingSeconds.get() != UNSET_DELAY;
	}

	protected int getRemainingSeconds() {
		return remainingSeconds.get();
	}

	private static final class SingletonHolder {

		private static final ShutdownHook INSTANCE = new ShutdownHook();
	}
}
