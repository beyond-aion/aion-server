package quest.linkgate_foundry;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Ritsu
 */
public class _16940DiarySecrets extends AbstractQuestHandler {

	public _16940DiarySecrets() {
		super(16940);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(802350).addOnQuestStart(questId);
		qe.registerQuestNpc(802350).addOnTalkEvent(questId);
		qe.registerQuestNpc(206361).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 802350) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 206361:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 1352);
							return false;
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
					break;
				case 802350:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 1)
								return sendQuestDialog(env, 2375);
							return false;
						}
						case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: {
							if (QuestService.collectItemCheck(env, true)) {
								changeQuestStep(env, 1, 1, true);
								return sendQuestDialog(env, 5);
							} else
								return closeDialogWindow(env);
						}
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 802350)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
