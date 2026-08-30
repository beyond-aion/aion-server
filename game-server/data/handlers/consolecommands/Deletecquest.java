package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.Quest;

/**
 * @author ginho1, Neon
 */
public class Deletecquest extends ConsoleCommand {

	public Deletecquest() {
		super("deletecquest", "Deletes a quest from the players quest list.");

		setSyntaxInfo("<quest link|ID> - Deletes the quest from your target's quest list (defaults to your character, if no player is targeted).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		Player player = admin.getTarget() instanceof Player target ? target : admin;
		Quest questCommand = ChatProcessor.getInstance().getCommand(Quest.class);
		questCommand.deleteQuest(admin, player, ChatUtil.getQuestId(params[0]));
	}
}
