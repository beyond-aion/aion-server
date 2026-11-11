package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Setinventorygrowth extends ConsoleCommand {

	public Setinventorygrowth() {
		super("setinventorygrowth", "Expands a cube.", """
			[value] - Adds 9 cube slots to your target (or yourself), the value is ignored.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		if (CubeExpandService.canExpand(player)) {
			CubeExpandService.npcExpand(player);
			sendInfo(admin, "9 cube slots successfully added to player " + player.getName() + "!");
			sendInfo(player, "Admin " + admin.getName() + " gave you a cube expansion!");
		} else {
			sendInfo(admin, "Cube expansion cannot be added to " + player.getName() + "!\nReason: player cube already fully expanded.");
		}
	}
}
