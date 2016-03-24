package consolecommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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
		super("givetitle");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		if (!ChatProcessor.getInstance().isCommandAllowed(admin, "set")) {
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command");
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "Select one player.");
			return;
		}

		final Player player = (Player) target;

		int titleId;
		try {
			titleId = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
			return;
		}
		if (titleId <= 263)
			setTitle(player, titleId);
		PacketSendUtility.sendMessage(admin, "Added " + titleId + " to" + player.getCommonData().getName());
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///addskill <named name>");
	}

	private void setTitle(Player player, int value) {

		if (player.getTitleList().contains(value)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_TITLE());
			return;
		}
		player.getTitleList().addTitle(value, false, 0);
	}
}
