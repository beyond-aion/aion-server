package playercommands;

import javolution.util.FastList;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ChatCommand;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Help extends PlayerCommand {

	public Help() {
		super("help", "Lists all commands you are allowed to use.");
	}

	@Override
	public void execute(Player player, String... params) {
		FastList<ChatCommand> allowedCommands = getAllowedCommands(player);

		if (!allowedCommands.isEmpty() && !(allowedCommands.size() == 1 && allowedCommands.contains(this))) {
			StringBuilder sb = new StringBuilder("List of available commands (" + allowedCommands.size() + "):");
			for (ChatCommand cmd : allowedCommands) {
				String desc = cmd.getDescription().isEmpty() ? "No description available." : cmd.getDescription();
				sb.append("\n\t[color:" + cmd.getAliasWithPrefix() + ";1 1 1] - " + desc);
			}
			sb.append("\nType in [color:<command> help;1 1 1] to get further information about a command.");
			sendInfo(player, sb.toString());
		}
		else {
			sendInfo(player, "You are not allowed to use any chat commands other than [color:" + getAliasWithPrefix() + ";1 1 1].");
		}
	}

	private FastList<ChatCommand> getAllowedCommands(Player player) {
		ChatProcessor cp = ChatProcessor.getInstance();
		FastList<ChatCommand> cmds = new FastList<ChatCommand>();

		for (ChatCommand cmd : cp.getCommandList()) {
			if (cp.isCommandAllowed(player, cmd) && !(cmd instanceof ConsoleCommand))
				cmds.add(cmd);
		}
		cmds.sort(null);
		
		return cmds;
	}
}
