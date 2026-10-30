package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.RemoveCd;

/**
 * @author Neon
 */
public class Itemcooltime extends ConsoleCommand {

	public Itemcooltime() {
		super("itemcooltime", "Removes cooldowns of all items and disables them until you log out.", """
			 - Removes and disables your item cooldowns.
			1 - Enables your item cooldowns again.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		// Retail has no way to undo the command but logging out, "1" is ours to match remove_skill_delay_all 1.
		if (params.length > 0 && params[0].equals("1")) {
			player.unsetCustomState(CustomPlayerState.NO_ITEM_COOLDOWN_MODE);
			sendInfo(player, "Cooldown times of all items have been recovered.");
			return;
		}
		RemoveCd.removeItemCooldowns(player);
		player.setCustomState(CustomPlayerState.NO_ITEM_COOLDOWN_MODE);
		sendInfo(player, "Cooldown times of all items have been disabled.");
	}
}
