package consolecommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Addquest extends ConsoleCommand {

	public Addquest() {
		super("addquest");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		int id;
		try {
			String quest = params[0];
			Pattern questId = Pattern.compile("\\[quest:([^%]+)]");
			Matcher result = questId.matcher(quest);
			if (result.find())
				id = Integer.parseInt(result.group(1));
			else
				id = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			info(admin, null);
			return;
		}

		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
		if (template == null) {
			PacketSendUtility.sendMessage(admin, "Quest " + id + " does not exist.");
			return;
		}

		if (QuestService.startQuest(new QuestEnv(null, player, id))) {
			PacketSendUtility.sendMessage(admin, "Quest started.");
			return;
		}

		StringBuilder denialReason = new StringBuilder();
		QuestService.checkStartConditions(player, id, false, 0, false, false, false, denialReason::append);
		PacketSendUtility.sendMessage(admin,
			"Quest not started" + (denialReason.isEmpty() ? "." : ", " + player.getName() + " fails: " + denialReason));
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax ///addquest <id quest>");
	}

}
