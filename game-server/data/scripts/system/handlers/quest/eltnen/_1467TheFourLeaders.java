package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Balthazar
 * @modified Pad
 */

public class _1467TheFourLeaders extends QuestHandler {

	private static final int npcId = 204045;
	private static final int[] mobIds = { 211696, 211697, 211698, 211699 };

	public _1467TheFourLeaders() {
		super(1467);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcId).addOnQuestStart(questId);
		qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		for (int mobId : mobIds)
			qe.registerQuestNpc(mobId).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == npcId) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_1:
						QuestService.startQuest(env);
						return sendQuestDialog(env, 1011);
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcId) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
					case SETPRO2:
						return defaultCloseDialog(env, 0, 2);
					case SETPRO3:
						return defaultCloseDialog(env, 0, 3);
					case SETPRO4:
						return defaultCloseDialog(env, 0, 4);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcId) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, qs.getQuestVarById(0) + 4);
					case SELECTED_QUEST_NOREWARD:
						QuestService.finishQuest(env, qs.getQuestVarById(0) - 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}

		int targetId = env.getTargetId();
		int var0 = qs.getQuestVarById(0);
		if (targetId == mobIds[0] && var0 == 1 || targetId == mobIds[1] && var0 == 2 || targetId == mobIds[2] && var0 == 3
			|| targetId == mobIds[3] && var0 == 4) {
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}

		return false;
	}
}
