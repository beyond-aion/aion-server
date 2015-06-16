package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Source
 */
public class GroupToMe extends AdminCommand {

	public GroupToMe() {
		super("grouptome");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			onFail(admin, null);
			return;
		}

		Player groupToMove = World.getInstance().findPlayer(Util.convertName(params[0]));
		if (groupToMove == null) {
			PacketSendUtility.sendMessage(admin, "The player is not online.");
			return;
		}

		if (!groupToMove.isInGroup2()) {
			PacketSendUtility.sendMessage(admin, groupToMove.getName() + " is not in group.");
			return;
		}

		for (Player target : groupToMove.getPlayerGroup2().getMembers())
			if (target != admin) {
				TeleportService2.teleportTo(target, admin.getWorldId(), admin.getInstanceId(), admin.getX(), admin.getY(),
					admin.getZ(), admin.getHeading());
				PacketSendUtility.sendMessage(target, "You have been summoned by " + admin.getName() + ".");
				PacketSendUtility.sendMessage(admin, "You summon " + target.getName() + ".");
			}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //grouptome <player>");
	}
}
