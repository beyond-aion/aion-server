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
		super("classup", "Promotes a players class.");

		setSyntaxInfo("<class> - Promotes your targets class to the one specified (defaults to your character, if nothing is targeted).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		Player player = admin.getTarget() instanceof Player target ? target : admin;
		String newClass = params[0];

		if (newClass.equalsIgnoreCase("fighter"))
			newClass = "GLADIATOR";
		else if (newClass.equalsIgnoreCase("knight"))
			newClass = "TEMPLAR";
		else if (newClass.equalsIgnoreCase("wizard"))
			newClass = "SORCERER";
		else if (newClass.equalsIgnoreCase("elementalist"))
			newClass = "SPIRIT_MASTER";

		PlayerClass playerClass;
		try {
			playerClass = PlayerClass.valueOf(newClass.toUpperCase());
			if (playerClass.isStartingClass())
				throw new IllegalArgumentException();
		} catch (IllegalArgumentException e) {
			sendInfo(admin, "Invalid player class.");
			return;
		}

		ClassChangeService.setClass(player, playerClass, false, true);
		sendInfo(admin, "You have promoted " + player.getName() + "'s class to " + playerClass.toString().toLowerCase() + ".");
	}
}
