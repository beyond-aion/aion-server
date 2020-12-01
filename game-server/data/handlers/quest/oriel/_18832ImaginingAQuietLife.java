package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Bobobear & Ritsu
 */
public class _18832ImaginingAQuietLife extends AbstractQuestHandler {

	public _18832ImaginingAQuietLife() {
		super(18832);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830365).addOnQuestStart(questId);
		qe.registerQuestNpc(830365).addOnTalkEvent(questId);
		qe.registerQuestNpc(830001).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 830365) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 830001:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 2375);
							return false;
						}
						case SELECT_QUEST_REWARD: {
							playQuestMovie(env, 801);
							changeQuestStep(env, 0, 0, true);
							return sendQuestDialog(env, 5);
						}
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 830001)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
