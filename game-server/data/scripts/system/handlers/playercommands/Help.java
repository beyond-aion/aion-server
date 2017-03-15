package playercommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
		List<ChatCommand> allowedCommands = findAllowedCommands(player);

		if (!allowedCommands.isEmpty() && !(allowedCommands.size() == 1 && allowedCommands.contains(this))) {
			allowedCommands.sort(Comparator.comparing(cmd -> cmd.getAliasWithPrefix().toLowerCase()));
			StringBuilder sb = new StringBuilder("List of available commands (" + allowedCommands.size() + "):");
			for (ChatCommand cmd : allowedCommands) {
				String desc = cmd.getDescription().isEmpty() ? "No description available." : cmd.getDescription();
				sb.append("\n\t" + ChatUtil.color(cmd.getAliasWithPrefix(), Color.WHITE) + " - " + desc);
			}
			sb.append("\nType <" + ChatUtil.color("command", Color.WHITE) + "> " + ChatUtil.color("help", Color.WHITE)
				+ " to get further information about a command.");
			sendInfo(player, sb.toString());
		} else {
			sendInfo(player, "You are not allowed to use any chat commands other than " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + ".");
		}
	}

	private List<ChatCommand> findAllowedCommands(Player player) {
		ChatProcessor cp = ChatProcessor.getInstance();
		List<ChatCommand> cmds = new ArrayList<>();

		for (ChatCommand cmd : cp.getCommandList()) {
			if (cp.isCommandAllowed(player, cmd) && !(cmd instanceof ConsoleCommand))
				cmds.add(cmd);
		}

		return cmds;
	}
}
