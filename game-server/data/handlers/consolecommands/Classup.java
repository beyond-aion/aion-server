package consolecommands;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Classup extends ConsoleCommand {

	public Classup() {
		super("classup", "Promotes a player's class.", """
			<class> - Promotes your target's class to the one specified (defaults to your character, if no player is targeted).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}
		Player player = admin.getTarget() instanceof Player target ? target : admin;
		PlayerClass playerClass = Changeclass.parsePlayerClass(params[0]);
		ClassChangeService.setClass(player, playerClass, false, true);
		sendInfo(admin, "You have promoted " + player.getName() + "'s class to " + playerClass.toString().toLowerCase() + ".");
	}
}
