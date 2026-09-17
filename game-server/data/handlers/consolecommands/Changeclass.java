package consolecommands;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Changeclass extends ConsoleCommand {

	public Changeclass() {
		super("changeclass", "Changes a player's class.", """
			<class> - Changes your target's class to the one specified (defaults to your character, if no player is targeted).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}
		Player player = admin.getTarget() instanceof Player target ? target : admin;
		PlayerClass playerClass = parsePlayerClass(params[0]);
		ClassChangeService.setClass(player, playerClass, false, true);
		sendInfo(admin, "You have changed " + player.getName() + "'s class to " + playerClass.toString().toLowerCase() +  ".");
	}

	protected static PlayerClass parsePlayerClass(String param) {
		return switch (param.toUpperCase()) {
			case "FIGHTER" -> PlayerClass.GLADIATOR;
			case "KNIGHT" -> PlayerClass.TEMPLAR;
			case "WIZARD" -> PlayerClass.SORCERER;
			case "ELEMENTALIST" -> PlayerClass.SPIRIT_MASTER;
			case String newClass -> PlayerClass.valueOf(newClass);
		};
	}
}
