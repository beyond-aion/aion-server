package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Altaress
 */
public class _3049BloodMarkstheSpot extends AbstractQuestHandler {

	public _3049BloodMarkstheSpot() {
		super(3049);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798211).addOnTalkEvent(questId);
		qe.registerQuestNpc(730149).addOnTalkEvent(questId);
		qe.registerQuestItem(182208034, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialogActionId == QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == 730149) {
				env.getVisibleObject().getController().deleteAndScheduleRespawn();
				return giveQuestItem(env, 182208034, 1);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798211) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182208034, 1);
					return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798211) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 5);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
