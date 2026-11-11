package consolecommands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.FinishedQuestCond;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Addquest extends ConsoleCommand {

	public Addquest() {
		super("addquest", "Starts a quest.", """
			<quest id> - Starts the quest for your target (or yourself), a quest link works too.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		int id;
		String quest = params[0];
		Pattern questId = Pattern.compile("\\[quest:([^%]+)]");
		Matcher result = questId.matcher(quest);
		if (result.find())
			id = Integer.parseInt(result.group(1));
		else
			id = Integer.parseInt(params[0]);

		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
		if (template == null) {
			sendInfo(admin, "There is no quest with ID " + id + ".");
			return;
		}

		QuestEnv env = new QuestEnv(null, player, id);

		if (QuestService.startQuest(env)) {
			sendInfo(admin, "Quest started.");
		} else {
			List<XMLStartCondition> preconditions = template.getXMLStartConditions();
			if (preconditions != null && preconditions.size() > 0) {
				for (XMLStartCondition condition : preconditions) {
					List<FinishedQuestCond> finisheds = condition.getFinishedPreconditions();
					if (finisheds != null && finisheds.size() > 0) {
						for (FinishedQuestCond fcondition : finisheds) {
							QuestState qs1 = player.getQuestStateList().getQuestState(fcondition.getQuestId());
							if (qs1 == null || qs1.getStatus() != QuestStatus.COMPLETE) {
								sendInfo(admin,
									player.getName() + " has to finish [quest:" + fcondition.getQuestId() + "] first.");
							}
						}
					}
				}
			}
			sendInfo(admin, "Quest not started, some preconditions failed.");
		}
	}

}
