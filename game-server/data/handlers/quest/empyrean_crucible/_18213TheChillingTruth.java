package quest.empyrean_crucible;

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
public class _18213TheChillingTruth extends AbstractQuestHandler {

	public _18213TheChillingTruth() {
		super(18213);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182212220, questId);
		qe.registerQuestNpc(205985).addOnQuestStart(questId);
		qe.registerQuestNpc(205985).addOnTalkEvent(questId);
		qe.registerQuestNpc(205316).addOnTalkEvent(questId);
		qe.registerQuestNpc(798604).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205985) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_ACCEPT_1:
						return sendQuestStartDialog(env, 182212219, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 205316) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SELECT2_1:
						removeQuestItem(env, 182212219, 1);
						giveQuestItem(env, 182212220, 1);
						return sendQuestDialog(env, 1353);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 798604) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1693);
						} else if (var == 3) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 3, 3, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798604) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
			removeQuestItem(env, 182212220, 1);
			changeQuestStep(env, 2, 3);
			return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}
