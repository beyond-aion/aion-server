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
		super("itemcooltime", "Removes cooldowns of all items and disables them until you log out.");
	}

	@Override
	public void execute(Player player, String... params) {
		RemoveCd.removeItemCooldowns(player);
		player.setCustomState(CustomPlayerState.NO_ITEM_COOLDOWN_MODE);
	}
}
