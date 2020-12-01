package quest.abyssal_splinter;

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
 * @author Rikka
 */
public class _30364RemembranceOfSpiritsPast extends AbstractQuestHandler {

	public _30364RemembranceOfSpiritsPast() {
		super(30364);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204058).addOnTalkEvent(questId);
		qe.registerQuestNpc(204108).addOnTalkEvent(questId);
		qe.registerQuestItem(182209822, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			return false;
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204108) { // Lanse
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
				}
			} else if (targetId == 204058) { // Sif
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true); // reward
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204058) { // Sif
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
			return HandlerResult.fromBoolean(QuestService.startQuest(env));
		}
		return HandlerResult.FAILED;
	}
}
