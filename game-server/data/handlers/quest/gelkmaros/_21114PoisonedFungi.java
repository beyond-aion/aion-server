package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _21114PoisonedFungi extends AbstractQuestHandler {

	public _21114PoisonedFungi() {
		super(21114);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799282).addOnQuestStart(questId);
		qe.registerQuestNpc(700727).addOnTalkEvent(questId);
		qe.registerQuestNpc(700729).addOnTalkEvent(questId);
		qe.registerQuestNpc(700728).addOnTalkEvent(questId);
		qe.registerQuestNpc(799282).addOnTalkEvent(questId);
		qe.registerQuestNpc(799405).addOnTalkEvent(questId);
		qe.registerQuestNpc(216563).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799282) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799282) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1011);
					else if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
					else if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2034);
				} else if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 0, 1, false, 10000, 10001);
				} else if (dialogActionId == SETPRO2) {
					giveQuestItem(env, 182207862, 1);
					return defaultCloseDialog(env, 1, 2);
				} else if (dialogActionId == SETPRO4) {
					return defaultCloseDialog(env, 3, 4);
				}
			} else if (targetId == 700727 || targetId == 700728) {
				if (qs.getQuestVarById(0) == 0)
					return true;
			} else if (targetId == 700729) {
				if (qs.getQuestVarById(0) == 2) {
					removeQuestItem(env, 182207862, 1);
					changeQuestStep(env, 2, 3);
					return true;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799282) {
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
		return defaultOnKillEvent(env, 216563, 4, true);
	}
}
