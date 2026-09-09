package admincommands;

import com.aionemu.gameserver.custom.instance.CustomInstanceRankEnum;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Estrayl
 */
public class CustomInstance extends AdminCommand {

	public CustomInstance() {
		super("cinstance", "Utility command for the custom instance.", """
			removecd - Removes the custom instance cooldown of selected player.
			getrank - Gets the current custom instance rank of selected player.
			setrank [newRank] - Changes the custom instance rank of selected player to given value.
			""");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length < 1) {
			sendInfo(player);
			return;
		}
		if (!(player.getTarget() instanceof Player target)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}
		if ("removecd".equalsIgnoreCase(params[0])) {
			if (CustomInstanceService.getInstance().resetEntryCooldown(target.getObjectId())) {
				sendInfo(player, "Removed custom instance cooldown for " + name(target) + ".");
			} else {
				sendInfo(player, name(target) + " does not need a reset.");
			}
		} else if ("getrank".equalsIgnoreCase(params[0])) {
			int rank = CustomInstanceService.getInstance().loadOrCreateRank(target.getObjectId()).getRank();
			sendInfo(player, name(target) + "'s current rank is " + CustomInstanceRankEnum.getRankDescription(rank) + " (" + rank + ").");
		} else if ("setrank".equalsIgnoreCase(params[0]) && params.length > 1) {
			int rank = Integer.parseInt(params[1]);
			CustomInstanceService.getInstance().changePlayerRank(target.getObjectId(), rank, 0);
			sendInfo(player, "Changed " + name(target) + " to " + rank + " which is equivalent to " + CustomInstanceRankEnum.getRankDescription(rank));
		} else {
			sendInfo(player);
		}
	}
}
