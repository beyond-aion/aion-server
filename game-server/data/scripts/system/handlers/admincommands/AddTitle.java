package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author xavier
 */
public class AddTitle extends AdminCommand {

	public AddTitle() {
		super("addtitle");
	}

	@Override
	public void execute(Player player, String... params) {
		if ((params.length < 1) || (params.length > 2)) {
			onFail(player, null);
			return;
		}

		int titleId = Integer.parseInt(params[0]);
		if ((titleId > 272) || (titleId < 1)) {
			PacketSendUtility.sendMessage(player, "title id " + titleId + " is invalid (must be between 1 and 272)");
			return;
		}

		Player target = null;
		if (params.length == 2) {
			target = World.getInstance().findPlayer(Util.convertName(params[1]));
			if (target == null) {
				PacketSendUtility.sendMessage(player, "player " + params[1] + " was not found");
				return;
			}
		}
		else {
			VisibleObject creature = player.getTarget();
			if (player.getTarget() instanceof Player) {
				target = (Player) creature;
			}

			if (target == null) {
				target = player;
			}
		}

		if (titleId < 272)
			titleId = target.getRace().getRaceId() * 272 + titleId;

		if (!target.getTitleList().addTitle(titleId, false, 0)) {
			PacketSendUtility.sendMessage(player, "you can't add title #" + titleId + " to "
				+ (target.equals(player) ? "yourself" : target.getName()));
		}
		else {
			if (target.equals(player)) {
				PacketSendUtility.sendMessage(player, "you added to yourself title #" + titleId);
			}
			else {
				PacketSendUtility.sendMessage(player, "you added to " + target.getName() + " title #" + titleId);
				PacketSendUtility.sendMessage(target, player.getName() + " gave you title #" + titleId);
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //addtitle title_id [playerName]");
	}
}
