package quest.abyssal_splinter;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rikka
 */
public class _30264ANecklacewithHistory extends AbstractQuestHandler {

	public _30264ANecklacewithHistory() {
		super(30264);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203706).addOnTalkEvent(questId);
		qe.registerQuestItem(182209802, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203706) { // Charna
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (QuestService.startQuest(env)) {
				changeQuestStep(env, 0, 0, true); // reward
				return HandlerResult.SUCCESS;
			}
		}
		return HandlerResult.FAILED;
	}
}
