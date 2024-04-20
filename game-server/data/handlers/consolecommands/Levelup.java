package consolecommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Levelup extends ConsoleCommand {

	public Levelup() {
		super("levelup", "Levels a player up.");

		setSyntaxInfo("<value> - Levels your target up by the specified number of levels.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (!(target instanceof Player)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		final Player player = (Player) target;
		int newLevel;
		try {
			newLevel = player.getLevel() + Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			sendInfo(admin, "Please specify the number of levels to subtract.");
			return;
		}

		if (newLevel < 1 || newLevel > GSConfig.PLAYER_MAX_LEVEL) {
			sendInfo(admin, "Invalid level.");
			return;
		}

		player.getCommonData().setLevel(newLevel);
		sendInfo(admin, "Set " + player.getName() + "'s level to " + player.getLevel());
	}
}
