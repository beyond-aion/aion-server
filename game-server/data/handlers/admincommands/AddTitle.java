package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.TitleTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author xavier
 */
public class AddTitle extends AdminCommand {

	public AddTitle() {
		super("addtitle", "Adds titles to players.", """
			<title ID> - Adds the title to your target (defaults to your character, if no player is targeted).
			<title ID> <player> - Adds the title to the specified player.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1 || params.length > 2) {
			sendInfo(player);
			return;
		}

		TitleTemplate titleTemplate = DataManager.TITLE_DATA.getTitleTemplate(Integer.parseInt(params[0]));
		if (titleTemplate == null) {
			sendInfo(player, "Invalid title ID.");
			return;
		}

		Player target;
		if (params.length == 2) {
			String playerName = Util.convertName(params[1]);
			target = World.getInstance().getPlayer(playerName);
			if (target == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
		} else {
			target = player.getTarget() instanceof Player playerTarget ? playerTarget : player;
		}

		if (!target.getTitleList().addTitle(titleTemplate.getTitleId(), false, 0)) {
			if (!target.equals(player))
				sendInfo(player, "Couldn't add title \"" + titleTemplate.getL10n() + "\" to " + name(target));
		} else {
			if (!target.equals(player)) {
				sendInfo(player, "Added title \"" + titleTemplate.getL10n() + "\" to " + name(target));
				sendInfo(target, name(player) + " gave you the title \"" + titleTemplate.getL10n() + "\"");
			}
		}
	}
}
