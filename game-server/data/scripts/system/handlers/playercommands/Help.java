package playercommands;

import java.awt.Color;
import java.util.Collection;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
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
		Collection<ChatCommand> allowedCommands = getAllowedCommands(player);

		if (!allowedCommands.isEmpty() && !(allowedCommands.size() == 1 && allowedCommands.contains(this))) {
			StringBuilder sb = new StringBuilder("List of available commands (" + allowedCommands.size() + "):");
			for (ChatCommand cmd : allowedCommands) {
				String desc = cmd.getDescription().isEmpty() ? "No description available." : cmd.getDescription();
				sb.append("\n\t" + ChatUtil.color(cmd.getAliasWithPrefix(), Color.WHITE) + " - " + desc);
			}
			sb.append("\nType in " + ChatUtil.color("<command> help", Color.WHITE) + " to get further information about a command.");
			sendInfo(player, sb.toString());
		} else {
			sendInfo(player, "You are not allowed to use any chat commands other than " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + ".");
		}
	}

	private Collection<ChatCommand> getAllowedCommands(Player player) {
		ChatProcessor cp = ChatProcessor.getInstance();
		Collection<ChatCommand> cmds = new FastTable<ChatCommand>().sorted();

		for (ChatCommand cmd : cp.getCommandList()) {
			if (cp.isCommandAllowed(player, cmd) && !(cmd instanceof ConsoleCommand))
				cmds.add(cmd);
		}

		return cmds;
	}
}
