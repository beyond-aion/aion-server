package admincommands;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.info.SystemInfo;
import com.aionemu.commons.utils.info.VersionInfo;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author lord_rex
 */
public class Sys extends AdminCommand {

	public Sys() {
		super("sys", "Shows and controls the system environment.");

		// @formatter:off
		setSyntaxInfo(
			"<info> - Shows general system information.",
			"<memory> [gc] - Shows memory usage statistics and optionally runs the garbage collector.",
			"<threadpool> - Shows thread pool manager info.",
			"<restart|shutdown> [delay] - Restarts or shuts down the server after the specified delay in seconds (default: uses delay from config)."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		if ("info".equalsIgnoreCase(params[0])) {
			sendInfo(player, "System information at: " + ServerTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss")));
			sendInfo(player, VersionInfo.commons.toString(GSConfig.TIME_ZONE_ID));
			sendInfo(player, GameServer.versionInfo.toString(GSConfig.TIME_ZONE_ID));
			sendInfo(player, SystemInfo.getSystemInfo());
		} else if ("memory".equalsIgnoreCase(params[0])) {
			if (params.length > 1 && "gc".equalsIgnoreCase(params[1])) {
				long time = System.currentTimeMillis();
				System.gc();
				sendInfo(player, "Garbage collection took " + (System.currentTimeMillis() - time) + " ms");
			}
			sendInfo(player, SystemInfo.getMemoryInfo());
		} else if ("threadpool".equalsIgnoreCase(params[0])) {
			List<String> stats = ThreadPoolManager.getInstance().getStats();
			for (String stat : stats) {
				sendInfo(player, stat.replaceAll("\t", ""));
			}
		} else if ("shutdown".equalsIgnoreCase(params[0])) {
			initShutdown(ExitCode.NORMAL, params.length == 1 ? null : params[1]);
		} else if ("restart".equalsIgnoreCase(params[0])) {
			initShutdown(ExitCode.RESTART, params.length == 1 ? null : params[1]);
		} else {
			sendInfo(player);
		}
	}

	private void initShutdown(int exitCode, String delay) {
		int delaySeconds = delay == null ? ShutdownConfig.DELAY : Integer.parseInt(delay);
		GameServer.initShutdown(exitCode, delaySeconds);
	}

}
