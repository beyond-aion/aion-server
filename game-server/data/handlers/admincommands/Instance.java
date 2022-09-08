package admincommands;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
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
		SM_SYSTEM_MESSAGE openingMsg;
		int maskId;
		long registrationPeriod;

		switch (instanceName.toLowerCase()) {
			case "dredgion" -> { // Only opening Terath Dredgion
				openingMsg = SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_03();
				maskId = 3;
				registrationPeriod = DREDGION_REGISTRATION_PERIOD;
			}
			case "eob" -> {
				openingMsg = SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War();
				maskId = 108;
				registrationPeriod = ENGULFED_OPHIDAN_BRIDGE_REGISTRATION_PERIOD;
			}
			case "id" -> {
				openingMsg = SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Fortress_Re();
				maskId = 111;
				registrationPeriod = IDGEL_DOME_REGISTRATION_PERIOD;
			}
			case "iww" -> {
				openingMsg = SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDF5_TD_war();
				maskId = 109;
				registrationPeriod = IRON_WALL_WARFRONT_REGISTRATION_PERIOD;
			}
			case "kb" -> {
				openingMsg = SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDKamar();
				maskId = 107;
				registrationPeriod = KAMAR_BATTLEFIELD_REGISTRATION_PERIOD;
			}
			default -> {
				openingMsg = null;
				maskId = 0;
				registrationPeriod = 0;
			}
		}
		if (maskId != 0) {
			if (PeriodicInstanceManager.getInstance().openRegistration(openingMsg, maskId, registrationPeriod))
				sendInfo(admin, "Registration for " + AutoGroupType.getAGTByMaskId(maskId) + " is now open.");
			else
				sendInfo(admin, "Registration for " + AutoGroupType.getAGTByMaskId(maskId) + " is already open.");
		} else {
			sendInfo(admin, "No instance found for " + instanceName);
		}
	}

	private void closeRegistration(Player admin, String instanceName) {
		int maskId;

		switch (instanceName.toLowerCase()) {
			case "dredgion" -> maskId = 3;
			case "eob" -> maskId = 108;
			case "id" -> maskId = 111;
			case "iww" -> maskId = 109;
			case "kb" -> maskId = 107;
			default -> maskId = 0;
		}

		if (maskId != 0) {
			if (PeriodicInstanceManager.getInstance().closeRegistration(maskId))
				sendInfo(admin, "Registration for " + AutoGroupType.getAGTByMaskId(maskId) + " is now closed.");
			else
				sendInfo(admin, "Registration for " + AutoGroupType.getAGTByMaskId(maskId) + " is not open.");
		} else {
			sendInfo(admin, "No instance found for " + instanceName);
		}
	}

}
