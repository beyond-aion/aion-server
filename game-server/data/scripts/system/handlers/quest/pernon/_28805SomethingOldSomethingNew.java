package quest.pernon;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _28805SomethingOldSomethingNew extends AbstractQuestHandler {

	public _28805SomethingOldSomethingNew() {
		super(28805);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830154).addOnQuestStart(questId);
		qe.registerQuestNpc(830154).addOnTalkEvent(questId);
		qe.registerQuestNpc(830521).addOnTalkEvent(questId);
		qe.registerQuestNpc(830662).addOnTalkEvent(questId);
		qe.registerQuestNpc(830663).addOnTalkEvent(questId);
		qe.registerQuestNpc(730525).addOnTalkEvent(questId);
		qe.registerQuestNpc(730522).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 830154) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 830521:
				case 830662:
				case 830663:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 1352);
							else if (var == 2)
								return sendQuestDialog(env, 2375);
							return false;
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 2, 2, true);
							return sendQuestDialog(env, 5);
						}
					}
					return false;
				case 730525:
				case 730522:
					switch (dialogActionId) {
						case USE_OBJECT: {
							if (var == 1)
								return sendQuestDialog(env, 1693);
							return false;
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2);
						}
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 830521:
				case 830662:
				case 830663:
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
