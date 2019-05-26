package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.GoTo;

/**
 * This command gets executed when clicking the "Transport" button in first tab of the GM console.
 * Functionally identical to //goto. Typing help or " " in the input field will show the syntax info or location list respectively.
 * 
 * @author Neon
 */
public class Teleportto extends ConsoleCommand {

	public Teleportto() {
		super("teleportto", "Teleports you to regions by name.");
	}

	@Override
	public String getSyntaxInfo() {
		GoTo goTo = ChatProcessor.getInstance().getCommand(GoTo.class);
		return goTo.getSyntaxInfo().replace(goTo.getAliasWithPrefix(), getAliasWithPrefix());
	}

	@Override
	public void execute(Player admin, String... params) {
		ChatProcessor.getInstance().getCommand(GoTo.class).execute(admin, params);
	}
}
