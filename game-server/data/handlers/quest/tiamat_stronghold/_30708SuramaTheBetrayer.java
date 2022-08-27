package quest.tiamat_stronghold;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * 1. Step: Talk to Surama.
 * 2. Step: Rescue 5 Captured Drakan Scientists.
 * 3. Step: Kill Brigade General Laksyaka
 * 4. Step: Talk to Murugan The Loyal.
 *
 * Second step is currently handled by {@link ai.instance.tiamatStrongHold.CapturedDrakanScientistAI}.
 *
 * @author Estrayl
 */
public class _30708SuramaTheBetrayer extends AbstractQuestHandler {

	private static final int START_NPC_ID = 800369; // Surama
	private static final int END_NPC_ID = 800438; // Murugan the Loyal
	private static final int TARGET_TO_KILL = 219356; // Brigade General Laksyaka

	public _30708SuramaTheBetrayer() {
		super(30708);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(TARGET_TO_KILL).addOnKillEvent(questId);
		qe.registerQuestNpc(START_NPC_ID).addOnQuestStart(questId);
		qe.registerQuestNpc(END_NPC_ID).addOnTalkEvent(questId);
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
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == END_NPC_ID) {
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

		if (env.getTargetId() == TARGET_TO_KILL && qs.getQuestVarById(0) == 5) {
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}
}
