package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.Stat;

/**
 * @author ginho1
 */
public class Attrbonus extends ConsoleCommand {

	public Attrbonus() {
		super("attrbonus", "Modifies your stats.");

		// @formatter:off
		setSyntaxInfo(
			"list - Lists all stats.",
			"<stat> - Shows active stat functions for the given stat.",
			"<stat> <value> - Sets the given stat to the given value.",
			"cancel - Cancels all active stat overrides.",
			"Stat parameters accept lowercase and abbreviated formats, such as flytime or flyt instead of FLY_TIME."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		Stat statCommand = ChatProcessor.getInstance().getCommand(Stat.class);
		if (params.length == 1 && "list".equalsIgnoreCase(params[0])) {
			statCommand.listStats(admin);
		} else if (params.length == 1 && "cancel".equalsIgnoreCase(params[0])) {
			statCommand.cancelStatOverrides(admin, admin);
		} else if (params.length == 1) {
			statCommand.showStatFunctions(admin, admin, params[0]);
		} else if (params.length == 2) {
			statCommand.setStat(admin, admin, params[0], Integer.parseInt(params[1]));
		} else {
			sendInfo(admin);
		}
	}
}
