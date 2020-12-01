package quest.gelkmaros;

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
 * @author Cheatkiller
 */
public class _21057FundinOldGrudge extends AbstractQuestHandler {

	public _21057FundinOldGrudge() {
		super(21057);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182207846, questId);
		qe.registerQuestNpc(799354).addOnTalkEvent(questId);
		qe.registerQuestNpc(296489).addOnKillEvent(questId);
		qe.registerQuestNpc(296490).addOnKillEvent(questId);
		qe.registerQuestNpc(296491).addOnKillEvent(questId);
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
					removeQuestItem(env, 182207846, 1);
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799354) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else if (dialogActionId == SELECT4) {
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					if (player.getInventory().getKinah() >= 15000000) {
						player.getInventory().decreaseKinah(15000000);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 3739);
				} else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 0)
				return defaultOnKillEvent(env, 296489, 0, 1);
			else if (var == 1)
				return defaultOnKillEvent(env, 296490, 1, 2);
			else if (var == 2)
				return defaultOnKillEvent(env, 296491, 2, true);
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
