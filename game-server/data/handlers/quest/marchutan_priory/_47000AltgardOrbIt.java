package quest.marchutan_priory;

import static com.aionemu.gameserver.model.DialogAction.*;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_DailyQuest_Ask_Mentor;

import com.aionemu.gameserver.configs.main.GroupConfig;
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
public class _47000AltgardOrbIt extends AbstractQuestHandler {

	public _47000AltgardOrbIt() {
		super(47000);
	}

	@Override
	public void register() {
		qe.addHandlerSideQuestDrop(questId, 700970, 182211033, 5, 100, 0);
		qe.registerQuestNpc(700970).addOnTalkEvent(questId);
		qe.registerQuestNpc(799864).addOnTalkEvent(questId);
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
			if (targetId == 700970) {
				if (player.isInGroup()) {
					PlayerGroup group = player.getPlayerGroup();
					if (group.getMembers().stream().anyMatch(member -> member.isMentor() && PositionUtil.isInRange(player, member, GroupConfig.GROUP_MAX_DISTANCE)))
						return true;
					PacketSendUtility.sendPacket(player, STR_MSG_DailyQuest_Ask_Mentor());
				}
			}
			if (targetId == 799864) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 0, 1, true, 5, 2716);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799864) {
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
