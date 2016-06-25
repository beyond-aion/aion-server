package admincommands;

import java.util.Arrays;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.dao.PlayerQuestListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.FinishedQuestCond;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author MrPoke
 * @reworked Neon
 * @modified Pad
 */
public class Quest extends AdminCommand {

	public Quest() {
		super("quest", "Handles quest states of your target.");
		
		setParamInfo(
			"[player] <quest> <reset|start|delete> - Resets/starts/deletes the specified quest.",
			"[player] <quest> <status> - Shows the quest status of the specified quest.",
			"[player] <quest> <set> <status> <var> [varNum] - Sets the specified quest state (default: apply var to all varNums, optional: set var to varNum [0-5]).",
			"[player] <quest> <setflags> <flags> - Sets the specified quest flags.",
			"[player] <quest> <dialog> <dialog_id> - Sends the specified quest dialog.",
			"Note: Player name parameters are optional. If missing, your current target will be taken."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		byte index = 0;
		Player target;
		int questId = ChatUtil.getQuestId(params[index]);
		if (questId == 0) {
			target = World.getInstance().findPlayer(Util.convertName(params[index]));

			if (target == null || !target.isOnline()) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_MSG_ASK_PCINFO_LOGOFF());
				return;
			}

			if (++index >= params.length) {
				sendInfo(admin);
				return;
			}

			questId = ChatUtil.getQuestId(params[index]);
		} else {
			if (!(admin.getTarget() instanceof Player)) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}

			target = (Player) admin.getTarget();
		}

		if (questId == 0 || DataManager.QUEST_DATA.getQuestById(questId) == null) {
			sendInfo(admin, "Invalid quest.");
			return;
		}

		if (++index >= params.length) {
			sendInfo(admin);
			return;
		}

		// quest and target are both valid at this point
		if (params[index].equalsIgnoreCase("reset")) {
			resetQuest(admin, target, questId);
		} else if (params[index].equalsIgnoreCase("start")) {
			startQuest(admin, target, questId);
		} else if (params[index].equalsIgnoreCase("delete")) {
			deleteQuest(admin, target, questId);
		} else if (params[index].equalsIgnoreCase("status")) {
			showQuestStatus(admin, target, questId);
		} else if (params[index].equalsIgnoreCase("set")) {
			QuestStatus status;
			int var;
			int varNum = -1;

			try {
				status = QuestStatus.valueOf(params[++index].toUpperCase());
			} catch (IllegalArgumentException e) {
				sendInfo(admin, "<status> is one of " + Arrays.toString(QuestStatus.values()));
				return;
			} catch (IndexOutOfBoundsException e) {
				sendInfo(admin);
				return;
			}

			try {
				var = Integer.valueOf(params[++index]);
			} catch (NumberFormatException e) {
				sendInfo(admin, "<var> must be an int value.");
				return;
			} catch (IndexOutOfBoundsException e) {
				sendInfo(admin);
				return;
			}

			if (++index < params.length) { // optional
				try {
					varNum = Integer.valueOf(params[index]);
					if (varNum < 0 || varNum > 5)
						throw new IllegalArgumentException();
				} catch (IllegalArgumentException e) { // also catches NumberFormatException
					sendInfo(admin, "[varNum] must be an int value from 0 to 5.");
					return;
				}
			}

			setQuestStatus(admin, target, questId, status, var, varNum);
		} else if (params[index].equalsIgnoreCase("setflags")) {
			int flags;

			try {
				flags = Integer.valueOf(params[++index]);
			} catch (IndexOutOfBoundsException | NumberFormatException e) {
				sendInfo(admin, "<flags> must be an int value.");
				return;
			}

			setQuestFlags(admin, target, questId, flags);
		} else if (params[index].equalsIgnoreCase("dialog")) {
			int dialogId;
			
			try {
				dialogId = Integer.valueOf(params[++index]);
			} catch (IndexOutOfBoundsException | NumberFormatException e) {
				sendInfo(admin, "<dialog_id> must be an int value.");
				return;
			}
			
			sendQuestDialog(admin, questId, dialogId);
		} else {
			sendInfo(admin);
		}
	}

	private void resetQuest(Player admin, Player target, int questId) {
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		if (qs == null || (qs.getStatus() != QuestStatus.START && qs.getStatus() != QuestStatus.REWARD)) {
			sendInfo(admin, "Only currently active quests can be reset.");
			return;
		}
		if (questId == 1006 || questId == 2008 || (qs.getStatus() == QuestStatus.REWARD && qs.getQuestVars().getQuestVars() == 1)) {
			sendInfo(admin, "Quest " + ChatUtil.quest(questId) + " can't be reset.");
			return;
		}
		if (qs.getQuestVarById(0) == 0) {
			sendInfo(admin, "Player " + target.getName() + "'s quest is already at the beginning.");
			return;
		}
		qs.setStatus(QuestStatus.START);
		qs.setQuestVar(0);
		PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
		sendInfo(admin, "Reset " + ChatUtil.quest(questId) + " for player " + target.getName() + ".");
	}

	private void startQuest(Player admin, Player target, int questId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if (template.getNpcFactionId() > 0) {
			startNpcFactionQuest(admin, target, questId, template.getNpcFactionId());
			return;
		} else if (QuestService.startQuest(new QuestEnv(null, target, questId, 0))) {
			sendInfo(admin, "Started " + ChatUtil.quest(questId) + " for player " + target.getName() + ".");
			return;
		}
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
			sendInfo(admin, "Quest is already started.");
		} else if (qs != null && qs.getStatus() == QuestStatus.COMPLETE && !qs.canRepeat()) {
			sendInfo(admin, "Quest is already completed.");
		} else {
			StringBuilder sb = new StringBuilder();
			List<XMLStartCondition> preconditions = template.getXMLStartConditions();
			if (preconditions != null) {
				for (XMLStartCondition condition : preconditions) {
					List<FinishedQuestCond> finisheds = condition.getFinishedPreconditions();
					if (finisheds != null) {
						for (FinishedQuestCond fcondition : finisheds) {
							QuestState qs1 = target.getQuestStateList().getQuestState(fcondition.getQuestId());
							if (qs1 == null || qs1.getStatus() != QuestStatus.COMPLETE) {
								sb.append("\n\t" + ChatUtil.quest(fcondition.getQuestId()));
							}
						}
					}
				}
			}
			sendInfo(admin, "Quest not started. "
				+ (sb.length() > 0 ? "These quest(s) must be completed first:" + sb.toString() : "Some preconditions failed."));
		}
	}

	private void startNpcFactionQuest(Player admin, Player target, int questId, int factionId) {
		NpcFaction faction = target.getNpcFactions().getActiveNpcFaction(false);
		if (faction == null || faction.getId() != factionId) {
			sendInfo(admin, "Player " + target.getName() + " is not registered to the organization for this quest.");
			return;
		}
		for (QuestTemplate template : DataManager.QUEST_DATA.getQuestsByNpcFaction(faction.getId(), target)) {
			if (template.getId() == questId) {
				// simulate daily reset
				faction.setActive(false);
				faction.setTime(-1);
				target.getNpcFactions().addNpcFaction(faction);
				faction.setActive(true);
				// set daily quest Id and time to avoid random quest
				faction.setState(ENpcFactionQuestState.NOTING);
				faction.setQuestId(questId);
				faction.setTime(faction.getTime() + 100);
				// send the daily quest to player
				target.getNpcFactions().sendDailyQuest();
				sendInfo(admin, "Started npc faction quest " + ChatUtil.quest(questId) + " for player " + target.getName() + ".");
				return;
			}
		}
		sendInfo(admin, "Quest not implemented or player level doesn't match.");
	}

	private void deleteQuest(Player admin, Player target, int questId) {
		if (admin.getAccessLevel() < AdminConfig.CMD_QUEST_ADV_PARAMS) {
			sendInfo(admin, "<You need access level " + AdminConfig.CMD_QUEST_ADV_PARAMS + " or higher to use this function>");
			return;
		}
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			sendInfo(admin, "Player " + target.getName() + " does not have that quest.");
			return;
		}
		qs.setPersistentState(PersistentState.DELETED);
		DAOManager.getDAO(PlayerQuestListDAO.class).store(target);
		target.getQuestStateList().removeQuest(questId);
		PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId));
		sendInfo(admin, "Deleted " + ChatUtil.quest(questId) + " for player " + target.getName() + ".");
	}

	private void showQuestStatus(Player admin, Player target, int questId) {
		if (admin.getAccessLevel() < AdminConfig.CMD_QUEST_ADV_PARAMS) {
			sendInfo(admin, "<You need access level " + AdminConfig.CMD_QUEST_ADV_PARAMS + " or higher to use this function>");
			return;
		}
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		StringBuilder sb = new StringBuilder("Player: " + target.getName() + ", quest: " + ChatUtil.quest(questId) + "\n\tQuest status: ");
		if (qs == null) {
			sb.append("NULL");
		} else {
			sb.append(qs.getStatus().toString());
			sb.append("\n\tQuest vars:");
			for (int i = 0; i <= 5; i++)
				sb.append(" " + qs.getQuestVarById(i));
			sb.append(", encoded [" + qs.getQuestVars().getQuestVars() + "]");
			sb.append("\n\tQuest flags: " + (qs.getFlags() & 0x3F) + " " + qs.getStepGroup()); // needs rework when flags are implemented like vars
			sb.append(", encoded [" + qs.getFlags() + "]");
		}
		sendInfo(admin, sb.toString());
	}

	private void setQuestStatus(Player admin, Player target, int questId, QuestStatus status, int var, int varNum) {
		if (admin.getAccessLevel() < AdminConfig.CMD_QUEST_ADV_PARAMS) {
			sendInfo(admin, "<You need access level " + AdminConfig.CMD_QUEST_ADV_PARAMS + " or higher to use this function>");
			return;
		}
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			qs = new QuestState(questId, status, 0, 0, 0, null, 0, null);
			target.getQuestStateList().addQuest(questId, qs);
		}
		qs.setStatus(status);
		if (varNum == -1)
			qs.setQuestVar(var);
		else
			qs.setQuestVarById(varNum, var);
		PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
		if (status == QuestStatus.COMPLETE) {
			qs.setCompleteCount(qs.getCompleteCount() + 1);
			target.getController().updateNearbyQuests();
		}
		sendInfo(admin, "Updated quest status of " + ChatUtil.quest(questId) + " for player " + target.getName() + ".");
	}

	private void setQuestFlags(Player admin, Player target, int questId, int flags) { // needs rework when flags are implemented like vars
		if (admin.getAccessLevel() < AdminConfig.CMD_QUEST_ADV_PARAMS) {
			sendInfo(admin, "<You need access level " + AdminConfig.CMD_QUEST_ADV_PARAMS + " or higher to use this function>");
			return;
		}
		QuestState qs = target.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			sendInfo(admin, "Flags can only be set for active quests.");
			return;
		}
		qs.setFlags(flags);
		PacketSendUtility.sendPacket(target, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
		sendInfo(admin, "Set " + target.getName() + "'s quest flags to " + flags + ".");
	}
	
	private void sendQuestDialog(Player admin, int questId, int dialogId) {
		if (admin.getAccessLevel() < AdminConfig.CMD_QUEST_ADV_PARAMS) {
			sendInfo(admin, "<You need access level " + AdminConfig.CMD_QUEST_ADV_PARAMS + " or higher to use this function>");
			return;
		}
		PacketSendUtility.sendPacket(admin, new SM_DIALOG_WINDOW(0, dialogId, questId));
		sendInfo(admin, "Dialog " + dialogId + " of Q" + questId + " sent!");
	}
}
