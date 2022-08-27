package quest.tiamat_stronghold;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Estrayl
 */
public class _30751DeathToTheDragonLord extends AbstractQuestHandler {

	private static final int START_NPC_ID = 804869; // Ginnie
	private static final int KAHRUN_NPC_ID = 800430; // Kahrun
	private static final int MARCHUTAN_NPC_ID = 800356; // Marchutan
	private static final int TIAMAT_NPC_ID = 219362; // Tiamat

	public _30751DeathToTheDragonLord() {
		super(30751);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(START_NPC_ID).addOnQuestStart(questId);
		qe.registerQuestNpc(START_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(KAHRUN_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(MARCHUTAN_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(TIAMAT_NPC_ID).addOnKillEvent(questId);

	}

	public boolean onDialogEvent(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == KAHRUN_NPC_ID && qs.getQuestVarById(0) == 1) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					changeQuestStep(env, 1, 2);
					return closeDialogWindow(env);
				}
			} else if (targetId == MARCHUTAN_NPC_ID && qs.getQuestVarById(0) == 2) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SET_SUCCEED) {
					changeQuestStep(env, 2, 2, true);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		if (env.getTargetId() == TIAMAT_NPC_ID && qs.getQuestVarById(0) == 0) {
			changeQuestStep(env, 0, 1);
			return true;
		}
		return false;
	}
}
