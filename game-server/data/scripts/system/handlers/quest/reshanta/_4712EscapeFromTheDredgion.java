package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _4712EscapeFromTheDredgion extends AbstractQuestHandler {

	public _4712EscapeFromTheDredgion() {
		super(4712);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279042).addOnQuestStart(questId);
		qe.registerQuestNpc(279042).addOnTalkEvent(questId);
		qe.registerQuestNpc(798327).addOnTalkEvent(questId);
		qe.registerQuestNpc(798328).addOnTalkEvent(questId);
		qe.registerQuestNpc(798329).addOnTalkEvent(questId);
		qe.registerQuestNpc(798330).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 279042) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798327 || targetId == 798328 || targetId == 798329 || targetId == 798330) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1011);
					}
				} else if (dialogActionId == SETPRO1) {
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().delete();
					return defaultCloseDialog(env, 0, 1, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279042) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 214823, 2, true);
	}
}
