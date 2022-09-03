package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.periodic.*;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl, Estrayl
 */
public class Instance extends AdminCommand {

	public Instance() {
		super("instance", "Activates or deactivates registration for pvp instances.");

		// @formatter:off
			setSyntaxInfo(
							"<open|close> dredgion - Opens/closes the registration for Dredgion (6vs6)",
							"<open|close> id - Opens/closes the registration for Idgel Dome (6vs6)",
							"<open|close> eob - Opens/closes the registration for Engulfed Ophidan Bridge (6vs6)",
							"<open|close> kb - Opens/closes the registration for Kamar Battlefield (12vs12)",
							"<open|close> iww - Opens/closes the registration for Iron Wall Warfront (24vs24)"
			);
			// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 2) {
			sendInfo(player);
			return;
		}
		if (params[0].equalsIgnoreCase("open"))
			openRegistration(player, params[1]);
		else if (params[0].equalsIgnoreCase("close"))
			closeRegistration(player, params[1]);
	}

	private void openRegistration(Player admin, String instanceName) {
		switch (instanceName.toLowerCase()) {
			case "dredgion":
				if (!DredgionService.getInstance().isRegisterAvailable()) {
					DredgionService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Dredgion is now open.");
				} else {
					sendInfo(admin, "Registration for Dredgion is already open.");
				}
				break;
			case "eob":
				if (!EngulfedOphidanBridgeService.getInstance().isRegisterAvailable()) {
					EngulfedOphidanBridgeService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Engulfed Ophidan Bridge is now open.");
				} else {
					sendInfo(admin, "Registration for Engulfed Ophidan Bridge is already open.");
				}
				break;
			case "id":
				if (!IdgelDomeService.getInstance().isRegisterAvailable()) {
					IdgelDomeService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Idgel Dome is now open.");
				} else {
					sendInfo(admin, "Registration for Idgel Dome is already open.");
				}
				break;
			case "iww":
				if (!IronWallWarfrontService.getInstance().isRegisterAvailable()) {
					IronWallWarfrontService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Iron Wall Warfront is now open.");
				} else {
					sendInfo(admin, "Registration for Iron Wall Warfront is already open.");
				}
				break;
			case "kb":
				if (!KamarBattlefieldService.getInstance().isRegisterAvailable()) {
					KamarBattlefieldService.getInstance().startRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Kamar Battlefield is now open.");
				} else {
					sendInfo(admin, "Registration for Kamar Battlefield is already open.");
				}
				break;
		}
	}

	private void closeRegistration(Player admin, String instanceName) {
		switch (instanceName.toLowerCase()) {
			case "dredgion":
				if (DredgionService.getInstance().isRegisterAvailable()) {
					DredgionService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Dredgion is now closed.");
				} else {
					sendInfo(admin, "Registration for Dredgion is not open.");
				}
				break;
			case "eob":
				if (EngulfedOphidanBridgeService.getInstance().isRegisterAvailable()) {
					EngulfedOphidanBridgeService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Engulfed Ophidan Bridge is now closed.");
				} else {
					sendInfo(admin, "Registration for Engulfed Ophidan Bridge is not open.");
				}
				break;
			case "id":
				if (IdgelDomeService.getInstance().isRegisterAvailable()) {
					IdgelDomeService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Idgel Dome is now closed.");
				} else {
					sendInfo(admin, "Registration for Idgel Dome is not open.");
				}
				break;
			case "iww":
				if (IronWallWarfrontService.getInstance().isRegisterAvailable()) {
					IronWallWarfrontService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Iron Wall Warfront is now closed.");
				} else {
					sendInfo(admin, "Registration for Iron Wall Warfront is not open.");
				}
				break;
			case "kb":
				if (KamarBattlefieldService.getInstance().isRegisterAvailable()) {
					KamarBattlefieldService.getInstance().stopRegistration();
					PacketSendUtility.sendMessage(admin, "Registration for Kamar Battlefield is now closed.");
				} else {
					sendInfo(admin, "Registration for Kamar Battlefield is not open.");
				}
				break;
		}
	}

}
