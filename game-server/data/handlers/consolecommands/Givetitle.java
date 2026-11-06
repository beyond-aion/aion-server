package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Givetitle extends ConsoleCommand {

	public Givetitle() {
		super("givetitle", "Adds a title.", "<title id> - Adds the title to your target (or yourself).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		if (!ChatProcessor.getInstance().isCommandAllowed(admin, "set")) {
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command");
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		int titleId = Integer.parseInt(params[0]);
		if (titleId <= 263)
			setTitle(player, titleId);
		PacketSendUtility.sendMessage(admin, "Added " + titleId + " to" + player.getCommonData().getName());
	}

	private void setTitle(Player player, int value) {

		if (player.getTitleList().contains(value)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_TITLE());
			return;
		}
		player.getTitleList().addTitle(value, false, 0);
	}
}
