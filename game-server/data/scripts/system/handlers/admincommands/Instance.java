package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.periodic.DredgionService;
import com.aionemu.gameserver.services.instance.periodic.EngulfedOphidianBridgeService;
import com.aionemu.gameserver.services.instance.periodic.IronWallFrontService;
import com.aionemu.gameserver.services.instance.periodic.KamarBattlefieldService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl
 */
public class Instance extends AdminCommand {

	private static final String SYNTAX = "Syntax: //instance start (kamar|dredgion|ophidian_bridge|iron_wall_front)>"
		+ "//instance end (kamar|dredgion|ophidian_bridge|iron_wall_front)";

	public Instance() {
		super("instance");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length == 0) {
			PacketSendUtility.sendMessage(player, SYNTAX);
			return;
		}
		String cmd = params[0];
		if (cmd.equalsIgnoreCase("start")) {
			if (params.length < 2) {
				PacketSendUtility.sendMessage(player, SYNTAX);
				return;
			}
			String instance = params[1];
			startInstance(player, instance);
		} else if (cmd.equalsIgnoreCase("end")) {
			if (params.length < 2) {
				PacketSendUtility.sendMessage(player, SYNTAX);
				return;
			}
			String instance = params[1];
			stopInstance(player, instance);
		}
	}

	private void startInstance(Player admin, String instanceName) {
		try {
			if (instanceName.equalsIgnoreCase("kamar")) {
				if (KamarBattlefieldService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " is already started, can't start twice.");
				} else {
					KamarBattlefieldService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " started successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("dredgion")) {
				if (DredgionService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " is already started, can't start twice.");
				} else {
					DredgionService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " started successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("ophidian_bridge")) {
				if (EngulfedOphidianBridgeService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " is already started, can't start twice.");
				} else {
					EngulfedOphidianBridgeService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " started successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("iron_wall_front")) {
				if (IronWallFrontService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " is already started, can't start twice.");
				} else {
					IronWallFrontService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " started successfully.");
				}
			} else {
				PacketSendUtility.sendMessage(admin, instanceName + " - unknown instance name.");
			}
		} catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Something went wrong, can't start requested instance. See \"error.log\" for more details");
		}
	}

	private void stopInstance(Player admin, String instanceName) {
		try {
			if (instanceName.equalsIgnoreCase("kamar")) {
				if (!KamarBattlefieldService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " isn't started, can't end.");
				} else {
					KamarBattlefieldService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " stopped successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("dredgion")) {
				if (!DredgionService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " isn't started, can't end.");
				} else {
					DredgionService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " stopped successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("ophidian_bridge")) {
				if (!EngulfedOphidianBridgeService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " isn't started, can't end.");
				} else {
					EngulfedOphidianBridgeService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " stopped successfully.");
				}
			} else if (instanceName.equalsIgnoreCase("iron_wall_front")) {
				if (!IronWallFrontService.getInstance().isRegisterAvailable()) {
					PacketSendUtility.sendMessage(admin, instanceName + " isn't started, can't end.");
				} else {
					IronWallFrontService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, instanceName + " stopped successfully.");
				}
			}
		} catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Something went wrong, can't start requested instance. See \"error.log\" for more details");
		}
	}

}
