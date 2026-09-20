package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.RemoveCd;

/**
 * @author Neon
 */
public class Itemcooltime extends ConsoleCommand {

	public Itemcooltime() {
		super("itemcooltime", "Removes cooldowns of all items.");
	}

	@Override
	public void execute(Player player, String... params) {
		RemoveCd.removeItemCooldowns(player);
	}
}
