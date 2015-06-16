package quest.sanctum;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 */
public class _3210RescueHaorunerk extends QuestHandler {
	private final static int questId = 3210;

	public _3210RescueHaorunerk() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798318).addOnQuestStart(questId);
		qe.registerQuestNpc(798318).addOnTalkEvent(questId);
		qe.registerQuestNpc(798331).addOnTalkEvent(questId);
		qe.registerQuestNpc(798332).addOnTalkEvent(questId);
		qe.registerQuestNpc(215056).addOnKillEvent(questId);
		qe.registerQuestNpc(215080).addOnKillEvent(questId);
	}


	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798318) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_REFUSE_1:
						return sendQuestDialog(env, 1004);
					case QUEST_ACCEPT_1:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 798332 && qs.getQuestVarById(0) == 0) {
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1011);
					case SELECT_ACTION_1012:
						return sendQuestDialog(env, 1012);
					case SETPRO1: 
						qs.setQuestVarById(0, 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
			}
		}

		if (targetId == 798331) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				}
				if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD && qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2) == 1) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
			}
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, 215056, 0, 1, 1) || defaultOnKillEvent(env, 215080, 0, 1, 2)) {
			return true;
		}
		return false;
	}
}