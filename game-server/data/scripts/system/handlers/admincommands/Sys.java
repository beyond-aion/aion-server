package admincommands;

import java.util.List;

import com.aionemu.commons.utils.AEInfos;
import com.aionemu.gameserver.ShutdownHook;
import com.aionemu.gameserver.ShutdownHook.ShutdownMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.AEVersions;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author lord_rex //sys info - System Informations //sys memory - Memory Informations //sys gc - Garbage Collector
 *         //sys shutdown <seconds> <announceInterval> - Call shutdown //sys restart <seconds> <announceInterval> - Call
 *         restart //sys threadpool - Thread pools info
 */
public class Sys extends AdminCommand {

	public Sys() {
		super("sys");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility
				.sendMessage(
					player,
					"Usage: //sys info | //sys memory | //sys gc | //sys restart <countdown time> <announce delay> | //sys shutdown <countdown time> <announce delay>");
			return;
		}

		if (params[0].equals("info")) {
			// Time
			PacketSendUtility.sendMessage(player, "System Informations at: " + AEInfos.getRealTime().toString());

			// Version Infos
			for (String line : AEVersions.getFullVersionInfo())
				PacketSendUtility.sendMessage(player, line);

			// OS Infos
			for (String line : AEInfos.getOSInfo())
				PacketSendUtility.sendMessage(player, line);

			// CPU Infos
			for (String line : AEInfos.getCPUInfo())
				PacketSendUtility.sendMessage(player, line);

			// JRE Infos
			for (String line : AEInfos.getJREInfo())
				PacketSendUtility.sendMessage(player, line);

			// JVM Infos
			for (String line : AEInfos.getJVMInfo())
				PacketSendUtility.sendMessage(player, line);
		}

		else if (params[0].equals("memory")) {
			// Memory Infos
			for (String line : AEInfos.getMemoryInfo())
				PacketSendUtility.sendMessage(player, line);
		}

		else if (params[0].equals("gc")) {
			long time = System.currentTimeMillis();
			PacketSendUtility.sendMessage(player, "RAM Used (Before): "
				+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576));
			System.gc();
			PacketSendUtility.sendMessage(player, "RAM Used (After): "
				+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576));
			System.runFinalization();
			PacketSendUtility.sendMessage(player, "RAM Used (Final): "
				+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576));
			PacketSendUtility.sendMessage(player,
				"Garbage Collection and Finalization finished in: " + (System.currentTimeMillis() - time) + " milliseconds...");
		}
		else if (params[0].equals("shutdown")) {
			try {
				int val = Integer.parseInt(params[1]);
				int announceInterval = Integer.parseInt(params[2]);
				ShutdownHook.getInstance().doShutdown(val, announceInterval, ShutdownMode.SHUTDOWN);
				PacketSendUtility.sendMessage(player, "Server will shutdown in " + val + " seconds.");
			}
			catch (ArrayIndexOutOfBoundsException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
		}
		else if (params[0].equals("restart")) {
			try {
				int val = Integer.parseInt(params[1]);
				int announceInterval = Integer.parseInt(params[2]);
				ShutdownHook.getInstance().doShutdown(val, announceInterval, ShutdownMode.RESTART);
				PacketSendUtility.sendMessage(player, "Server will restart in " + val + " seconds.");
			}
			catch (ArrayIndexOutOfBoundsException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(player, "Numbers only!");
			}
		}
		else if (params[0].equals("threadpool")) {
			List<String> stats = ThreadPoolManager.getInstance().getStats();
			for (String stat : stats) {
				PacketSendUtility.sendMessage(player, stat.replaceAll("\t", ""));
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility
			.sendMessage(
				player,
				"Usage: //sys info | //sys memory | //sys gc | //sys restart <countdown time> <announce delay> | //sys shutdown <countdown time> <announce delay>");
	}

}
