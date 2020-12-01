package quest.silentera_canyon;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _30154FleshtoStone extends AbstractQuestHandler {

	public _30154FleshtoStone() {
		super(30154);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799234).addOnQuestStart(questId);
		qe.registerQuestNpc(799234).addOnTalkEvent(questId);
		qe.registerQuestNpc(204433).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799234) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799234)
				return sendQuestEndDialog(env);
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204433) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						if (var == 0)
							return defaultCloseDialog(env, 0, 1);
				}
			}
			if (targetId == 799234) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 2375);
						return false;
					case SELECT_QUEST_REWARD:
						if (var == 1)
							return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		}
		return false;
	}
}
