package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _11006TestingTheWaters extends AbstractQuestHandler {

	public _11006TestingTheWaters() {
		super(11006);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182206704, questId);
		qe.registerQuestItem(182206705, questId);
		qe.registerQuestNpc(798940).addOnQuestStart(questId);
		qe.registerQuestNpc(798940).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798940) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182206704, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798940) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					giveQuestItem(env, 182206705, 1);
					removeQuestItem(env, 182206706, 1);
					return defaultCloseDialog(env, 1, 2);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798940) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				removeQuestItem(env, 182206707, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (item.getItemId() == 182206704) {
				return HandlerResult.fromBoolean(useQuestItem(env, item, 0, 1, false, 182206706, 1));
			} else if (item.getItemId() == 182206705) {
				return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 2, true, 182206707, 1));
			}
		}
		return HandlerResult.FAILED;
	}
}
