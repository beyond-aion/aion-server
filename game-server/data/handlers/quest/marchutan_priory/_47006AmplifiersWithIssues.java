package quest.marchutan_priory;

import static com.aionemu.gameserver.model.DialogAction.*;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_DailyQuest_Ask_Mentor;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Cheatkiller
 */
public class _47006AmplifiersWithIssues extends AbstractQuestHandler {

	public _47006AmplifiersWithIssues() {
		super(47006);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(700972).addOnTalkEvent(questId);
		qe.registerQuestNpc(799872).addOnTalkEvent(questId);
		qe.registerQuestNpc(217175).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 217175, 0, 5);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialogActionId == QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		}

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 700972) {
				if (player.isInGroup()) {
					PlayerGroup group = player.getPlayerGroup();
					if (group.getMembers().stream().anyMatch(member -> member.isMentor() && PositionUtil.isInRange(player, member, GroupConfig.GROUP_MAX_DISTANCE))) {
						Npc npc = (Npc) env.getVisibleObject();
						npc.getController().deleteAndScheduleRespawn();
						spawnForFiveMinutes(217175, npc.getPosition());
						return true;
					}
					PacketSendUtility.sendPacket(player, STR_MSG_DailyQuest_Ask_Mentor());
				}
			}
			if (targetId == 799872) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 5) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 5, 5, true, true);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799872) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
