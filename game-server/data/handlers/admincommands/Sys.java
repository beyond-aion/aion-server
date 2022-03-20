package admincommands;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.info.SystemInfoUtil;
import com.aionemu.commons.utils.info.VersionInfoUtil;
import com.aionemu.gameserver.GameServer;
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
			sendInfo(player, "System Information at: " + ServerTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss")));
			sendInfo(player, VersionInfoUtil.getVersionInfo(GameServer.class).getAllInfo());
			sendInfo(player, SystemInfoUtil.getOsInfo());
			sendInfo(player, SystemInfoUtil.getJvmCpuInfo());
			sendInfo(player, SystemInfoUtil.getJreInfo());
			sendInfo(player, SystemInfoUtil.getJvmInfo());
		} else if ("memory".equalsIgnoreCase(params[0])) {
			if (params.length == 1) {
				sendInfo(player, SystemInfoUtil.getMemoryInfo());
			} else if (params[1].equalsIgnoreCase("gc")) {
				long time = System.currentTimeMillis();
				sendInfo(player, "RAM Used (Before): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				System.gc();
				sendInfo(player, "RAM Used (After): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				System.runFinalization();
				sendInfo(player, "RAM Used (Final): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				sendInfo(player, "Garbage Collection and Finalization finished in " + ((System.currentTimeMillis() - time) / 1000) + " second(s)");
			} else {
				sendInfo(player);
			}
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
