package playercommands;

import java.util.Iterator;
import java.util.List;

import javolution.util.FastList;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
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
		List<ChatCommand> allowedCommands = getAllowedCommands(player);

		if (!allowedCommands.isEmpty() && !(allowedCommands.size() == 1 && allowedCommands.contains(this))) {
			PacketSendUtility.sendMessage(player, "List of available commands:");
			for (ChatCommand cmd : allowedCommands) {
				String desc = cmd.getDescription().isEmpty() ? "No description available." : cmd.getDescription();
				PacketSendUtility.sendMessage(player, "  " + cmd.getAliasWithPrefix() + " - " + desc);
			}
		}
		else {
			PacketSendUtility.sendMessage(player, "You are not allowed to use any chat commands other than " + getAliasWithPrefix() + ".");
		}
	}

	private List<ChatCommand> getAllowedCommands(Player player) {
		ChatProcessor cp = ChatProcessor.getInstance();
		Iterator<ChatCommand> iter = cp.getCommandList().iterator();
		List<ChatCommand> cmds = new FastList<ChatCommand>();

		while (iter.hasNext()) {
			ChatCommand cmd = iter.next();
			if (cp.isCommandAllowed(player, cmd) && !(cmd instanceof ConsoleCommand))
				cmds.add(cmd);
		}
		cmds.sort(null);;
		
		return cmds;
	}
}
