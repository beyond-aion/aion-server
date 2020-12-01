package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _3036LetSeeWhatItDoes extends AbstractQuestHandler {

	public _3036LetSeeWhatItDoes() {
		super(3036);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798155).addOnQuestStart(questId);
		qe.registerQuestNpc(798155).addOnTalkEvent(questId);
		qe.registerQuestNpc(700398).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798155) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182208026, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 700398) {
				removeQuestItem(env, 182208026, 1);
				return useQuestObject(env, 0, 1, true, false);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798155) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}
