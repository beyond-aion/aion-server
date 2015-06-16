package admincommands;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.FinishedQuestCond;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author MrPoke
 */
public class Quest extends AdminCommand {

	public Quest() {
		super("quest");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "syntax //quest <start|set|setflags|show|delete|daily>");
			return;
		}
		Player target = null;
		VisibleObject creature = admin.getTarget();
		if (admin.getTarget() instanceof Player) {
			target = (Player) creature;
		}

		if (target == null) {
			PacketSendUtility.sendMessage(admin, "Incorrect target!");
			return;
		}

		if (params[0].equals("start")) {
			if (params.length != 2) {
				PacketSendUtility.sendMessage(admin, "syntax //quest start <questId>");
				return;
			}
			int id;
			try {
				String quest = params[1];
				Pattern questId = Pattern.compile("\\[quest:([^%]+)]");
				Matcher result = questId.matcher(quest);
				if (result.find())
					id = Integer.parseInt(result.group(1));
				else
					id = Integer.parseInt(params[1]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "syntax //quest start <questId>");
				return;
			}

			QuestEnv env = new QuestEnv(null, target, id, 0);

			if (QuestService.startQuest(env)) {
				PacketSendUtility.sendMessage(admin, "Quest started.");
			}
			else {
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
		else if (params[0].equals("set")) {
			int questId, var;
			int varNum = 0;
			QuestStatus questStatus;
			try {
				String quest = params[1];
				Pattern id = Pattern.compile("\\[quest:([^%]+)]");
				Matcher result = id.matcher(quest);
				if (result.find())
					questId = Integer.parseInt(result.group(1));
				else
					questId = Integer.parseInt(params[1]);

				String statusValue = params[2];
				switch (statusValue) {
			   	  case "START":
					 questStatus = QuestStatus.START;
					 break;
			   	  case "NONE":
					 questStatus = QuestStatus.NONE;
					 break;
			   	  case "COMPLETE":
					 questStatus = QuestStatus.COMPLETE;
					 break;
			   	  case "REWARD":
					 questStatus = QuestStatus.REWARD;
					 break;
			   	  default:
					 PacketSendUtility.sendMessage(admin, "<status is one of START, NONE, REWARD, COMPLETE>");
					 return;
			   }
				var = Integer.valueOf(params[3]);
				if (params.length == 5 && params[4] != null && !"".equals(params[4])) {
					varNum = Integer.valueOf(params[4]);
				}
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "syntax //quest set <questId status var [varNum]>");
				return;
			}
			QuestState qs = target.getQuestStateList().getQuestState(questId);
			if (qs == null) {
				qs = new QuestState(questId, questStatus, 0, 0, new Timestamp(0), 0, new Timestamp(0));
				target.getQuestStateList().addQuest(questId, qs);				
				PacketSendUtility.sendMessage(admin, "<QuestState has been newly initialized.>");
				return;
			}
			qs.setStatus(questStatus);
			if (varNum != 0) {
				qs.setQuestVarById(varNum, var);
			}
			else {
				qs.setQuestVar(var);
			}
			PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
			if (questStatus == QuestStatus.COMPLETE) {
				qs.setCompleteCount(qs.getCompleteCount() + 1);
				target.getController().updateNearbyQuests();
			}
		}
		else if (params[0].equals("delete")) {
			if (params.length != 2) {
				PacketSendUtility.sendMessage(admin, "syntax //quest delete <quest id>");
				return;
			}
			int id;
			try {
				id = Integer.valueOf(params[1]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "syntax //quest delete <quest id>");
				return;
			}

			QuestStateList list = admin.getQuestStateList();
			if (list == null || list.getQuestState(id) == null) {
				PacketSendUtility.sendMessage(admin, "Quest not deleted.");
			}
			else {
				QuestState qs = list.getQuestState(id);
				qs.setQuestVar(0);
				qs.setCompleteCount(0);
				qs.setFlags(0);
				qs.setStatus(null);
				if (qs.getPersistentState() != PersistentState.NEW)
					qs.setPersistentState(PersistentState.DELETED);
				QuestEngine.getInstance().sendCompletedQuests(admin);
				admin.getController().updateNearbyQuests();
			}
		}
		else if (params[0].equals("setflags")) {
			int flags, questId;
			QuestState qs;
			if (params.length != 3) {
				PacketSendUtility.sendMessage(admin, "syntax //quest setflags <quest id> <flags>");
				return;
			}
			try {
				questId = Integer.valueOf(params[1]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "quest id is not a number!");
				return;
			}
			QuestStateList list = admin.getQuestStateList();
			if (list == null || list.getQuestState(questId) == null) {
				PacketSendUtility.sendMessage(admin, "No such quest!");
				return;
			}
			else {
				qs = list.getQuestState(questId);
			}
			if (qs.getStatus() != QuestStatus.START) {
				PacketSendUtility.sendMessage(admin, "Flags for not START are not supported now!");
				return;
			}
			try {
				flags = Integer.valueOf(params[2]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "flags is not a number!");
				return;
			}
			qs.setFlags(flags);
			PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId, qs.getStatus().value(), 0, flags));
		}
		else if (params[0].equals("show")) {
			if (params.length != 2) {
				PacketSendUtility.sendMessage(admin, "syntax //quest show <quest id>");
				return;
			}
			ShowQuestInfo(target, admin, params[1]);
		}
		else if (params[0].equals("daily")) {
			if (params.length != 2) {
				PacketSendUtility.sendMessage(admin, "syntax //quest daily <quest id>");
				return;
			}
			int id;
			try {
				id = Integer.valueOf(params[1]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "syntax //quest daily <quest id>");
				return;
			}
			NpcFaction faction = target.getNpcFactions().getActiveNpcFaction(false);
			if (faction == null) {
				PacketSendUtility.sendMessage(admin, "Player does not have any faction registered!");
				return;
			}
			List<QuestTemplate> quests = DataManager.QUEST_DATA.getQuestsByNpcFaction(faction.getId(), target);
			if (quests.isEmpty()) {
				PacketSendUtility.sendMessage(admin, "There is no quests available!");
				return;
			}
			boolean found = false;
			for (QuestTemplate template : quests) {
				if (template.getId() == id) {
					found = true; 
					break;
				}
			}
			if (!found) {
				PacketSendUtility.sendMessage(admin, "No such quest id or level is not permitted!");
				return;
			}
			faction.setActive(false);
			faction.setTime(-1);
			target.getNpcFactions().addNpcFaction(faction);
			faction.setActive(true);
			faction.setState(ENpcFactionQuestState.NOTING);
			faction.setTime(faction.getTime() + 10);
			faction.setQuestId(id);
			target.getNpcFactions().sendDailyQuest();
		}
		else
			onFail(admin, null);
	}

	private void ShowQuestInfo(Player player, Player admin, String param) {
		int id;
		try {
			id = Integer.valueOf(param);
		}
		catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "syntax //quest show <quest id>");
			return;
		}
		QuestState qs = player.getQuestStateList().getQuestState(id);
		if (qs == null) {
			PacketSendUtility.sendMessage(admin, "Quest state: NULL");
		}
		else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 5; i++)
				sb.append(Integer.toString(qs.getQuestVarById(i)) + " ");
			PacketSendUtility.sendMessage(admin, "Quest state: " + qs.getStatus().toString() + "; vars: " + sb.toString()
					+ qs.getQuestVarById(5));
			sb.setLength(0);
			sb = null;
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //quest <start|set|setflags|show|delete|daily>");
	}

}