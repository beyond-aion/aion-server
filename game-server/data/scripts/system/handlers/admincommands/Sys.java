package admincommands;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.info.SystemInfoUtil;
import com.aionemu.commons.utils.info.VersionInfoUtil;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.ShutdownHook;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author lord_rex
 */
public class Sys extends AdminCommand {

	public Sys() {
		super("sys", "Shows and controls the system environment");

		// @formatter:off
		setSyntaxInfo(
			"<info> - Shows general system information",
			"<memory> [gc] - Shows memory usage statistics and optionally runs the garbage collector",
			"<threadpool> - Shows thread pool manager info",
			"<restart> <delay> - Restarts the server after the specified delay in seconds.",
			"<shutdown> <delay> - Shuts down the server after the specified delay in seconds."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		if (params[0].equals("info")) {
			PacketSendUtility.sendMessage(player, "System Information at: " + ServerTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss")));
			for (String line : VersionInfoUtil.getVersionInfo(GameServer.class).getAllInfo())
				PacketSendUtility.sendMessage(player, line);
			for (String line : SystemInfoUtil.getOsInfo())
				PacketSendUtility.sendMessage(player, line);
			for (String line : SystemInfoUtil.getJvmCpuInfo())
				PacketSendUtility.sendMessage(player, line);
			for (String line : SystemInfoUtil.getJreInfo())
				PacketSendUtility.sendMessage(player, line);
			for (String line : SystemInfoUtil.getJvmInfo())
				PacketSendUtility.sendMessage(player, line);
		} else if (params[0].equals("memory")) {
			if (params.length == 1) {
				for (String line : SystemInfoUtil.getMemoryInfo())
					PacketSendUtility.sendMessage(player, line);
			} else if (params[1].equals("gc")) {
				long time = System.currentTimeMillis();
				PacketSendUtility.sendMessage(player,
					"RAM Used (Before): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				System.gc();
				PacketSendUtility.sendMessage(player,
					"RAM Used (After): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				System.runFinalization();
				PacketSendUtility.sendMessage(player,
					"RAM Used (Final): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MiB");
				PacketSendUtility.sendMessage(player,
					"Garbage Collection and Finalization finished in " + ((System.currentTimeMillis() - time) / 1000) + " second(s)");
			} else {
				sendInfo(player);
				return;
			}
		} else if (params[0].equals("threadpool")) {
			List<String> stats = ThreadPoolManager.getInstance().getStats();
			for (String stat : stats) {
				PacketSendUtility.sendMessage(player, stat.replaceAll("\t", ""));
			}
		} else if (params[0].equals("shutdown")) {
			try {
				int val = Integer.parseInt(params[1]);
				ShutdownHook.getInstance().shutdown(val, ExitCode.CODE_NORMAL);
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
		} else if (params[0].equals("restart")) {
			try {
				int val = Integer.parseInt(params[1]);
				ShutdownHook.getInstance().shutdown(val, ExitCode.CODE_RESTART);
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
		} else {
			sendInfo(player);
		}
	}

}
