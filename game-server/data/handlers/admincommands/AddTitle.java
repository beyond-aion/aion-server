package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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
		super("addtitle", "Adds titles to players.");

		setSyntaxInfo("<titleId> [playerName] - Adds the title to your target or the specified player.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1 || params.length > 2) {
			sendInfo(player);
			return;
		}

		TitleTemplate titleTemplate = DataManager.TITLE_DATA.getTitleTemplate(NumberUtils.toInt(params[0]));
		if (titleTemplate == null) {
			sendInfo(player, "Invalid title id.");
			return;
		}

		Player target = null;
		if (params.length == 2) {
			String playerName = Util.convertName(params[1]);
			target = World.getInstance().getPlayer(playerName);
			if (target == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
		} else {
			VisibleObject creature = player.getTarget();
			if (player.getTarget() instanceof Player) {
				target = (Player) creature;
			}

			if (target == null) {
				target = player;
			}
		}

		if (!target.getTitleList().addTitle(titleTemplate.getTitleId(), false, 0)) {
			if (!target.equals(player))
				sendInfo(player, "Couldn't add title \"" + titleTemplate.getL10n() + "\" to " + target);
		} else {
			if (!target.equals(player)) {
				sendInfo(player, "Added title \"" + titleTemplate.getL10n() + "\" to " + target);
				sendInfo(target, player.getName(true) + " gave you the title \"" + titleTemplate.getL10n() + "\"");
			}
		}
	}
}
