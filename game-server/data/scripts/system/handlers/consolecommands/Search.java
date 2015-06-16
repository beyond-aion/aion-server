package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Search extends ConsoleCommand {

	public Search() {
		super("search");
	}

	@Override
	public void execute(Player admin, String... params) {
		PacketSendUtility.sendMessage(admin, "Command not implemented.");
		return;
	}
}