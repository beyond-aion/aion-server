package consolecommands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.FinishedQuestCond;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
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

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

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

		QuestEnv env = new QuestEnv(null, player, id);

		if (QuestService.startQuest(env)) {
			PacketSendUtility.sendMessage(admin, "Quest started.");
		} else {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
			List<XMLStartCondition> preconditions = template.getXMLStartConditions();
			if (preconditions != null && preconditions.size() > 0) {
				for (XMLStartCondition condition : preconditions) {
					List<FinishedQuestCond> finisheds = condition.getFinishedPreconditions();
					if (finisheds != null && finisheds.size() > 0) {
						for (FinishedQuestCond fcondition : finisheds) {
							QuestState qs1 = admin.getQuestStateList().getQuestState(fcondition.getQuestId());
							if (qs1 == null || qs1.getStatus() != QuestStatus.COMPLETE) {
								PacketSendUtility.sendMessage(admin, "You have to finish " + fcondition.getQuestId() + " first!");
							}
						}
					}
				}
			}
			PacketSendUtility.sendMessage(admin, "Quest not started. Some preconditions failed");
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax ///addquest <id quest>");
	}

}
