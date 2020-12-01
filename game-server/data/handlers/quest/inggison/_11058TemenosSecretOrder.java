package quest.inggison;

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
public class _11058TemenosSecretOrder extends AbstractQuestHandler {

	public _11058TemenosSecretOrder() {
		super(11058);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182206844, questId);
		qe.registerQuestNpc(799049).addOnTalkEvent(questId);
		qe.registerQuestNpc(296493).addOnKillEvent(questId);
		qe.registerQuestNpc(296494).addOnKillEvent(questId);
		qe.registerQuestNpc(296495).addOnKillEvent(questId);
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
					removeQuestItem(env, 182206844, 1);
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799049) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else if (dialogActionId == SELECT4) {
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					if (player.getInventory().getKinah() >= 20000000) {
						player.getInventory().decreaseKinah(20000000);
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
				return defaultOnKillEvent(env, 296493, 0, 1);
			else if (var == 1)
				return defaultOnKillEvent(env, 296494, 1, 2);
			else if (var == 2)
				return defaultOnKillEvent(env, 296495, 2, true);
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
