package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.craft.Profession;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.craft.RelinquishCraftStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author synchro2, Neon
 */
public class RelinquishCraft extends AdminCommand {

	public RelinquishCraft() {
		super("relinquishcraft", "Removes a players crafting expert or master status.");

		// @formatter:off
		setSyntaxInfo(
			"<skillId> <expert|master> - Removes your target's master or expert status for the given crafting skill. Affects your own character if no player is targeted.",
			"<name> <skillId> <expert|master> - Removes the player's master or expert status for the given crafting skill."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		int i = 0;
		Player target;
		if (params.length == 3) {
			String playerName = Util.convertName(params[i++]);
			target = World.getInstance().getPlayer(playerName);
			if (target == null) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
		} else {
			target = admin.getTarget() instanceof Player player ? player : admin;
		}

		Profession profession = Profession.getBySkillId(NumberUtils.toInt(params[i++]));
		if (profession == null || !profession.isCrafting()) {
			sendInfo(admin, "Invalid skill ID.");
			return;
		}

		if ("expert".equalsIgnoreCase(params[i])) {
			if (RelinquishCraftStatus.relinquishExpertStatus(target, profession, 0))
				sendInfo(admin, "Successfully removed expert status for " + profession);
			else
				sendInfo(admin, target.getName() + " doesn't have " + profession + " on expert.");
		} else if ("master".equalsIgnoreCase(params[i])) {
			if (RelinquishCraftStatus.relinquishMasterStatus(target, profession, 0))
				sendInfo(admin, "Successfully removed master status for " + profession);
			else
				sendInfo(admin, target.getName() + " doesn't have " + profession + " on master.");
		} else
			sendInfo(admin);
	}
}
