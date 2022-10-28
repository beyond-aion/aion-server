package quest.the_eternal_bastion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _18035ShebasSurveillance extends AbstractQuestHandler {

	private static final int TALK_NPC_ID = 801281; // Demades
	private static final int END_NPC_ID = 804709; // Brunte
	private static final int QUEST_ITEM_ID = 182213483; // Dropped Letter

	public _18035ShebasSurveillance() {
		super(18035);
	}

	@Override
	public void register() {
		qe.registerQuestItem(QUEST_ITEM_ID, questId);
		qe.registerQuestNpc(TALK_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(END_NPC_ID).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0 && dialogActionId == QUEST_ACCEPT_SIMPLE) {
				QuestService.startQuest(env);
				return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case TALK_NPC_ID:
					return switch (dialogActionId) {
						case QUEST_SELECT -> sendQuestDialog(env, 1352);
						case SELECT2_1 -> sendQuestDialog(env, 1353);
						case SETPRO1 -> defaultCloseDialog(env, 0, 1);
						case FINISH_DIALOG -> sendQuestSelectionDialog(env);
						default -> false;
					};
				case END_NPC_ID:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							removeQuestItem(env, QUEST_ITEM_ID, 1);
							changeQuestStep(env, 1, 1, true);
							return sendQuestDialog(env, 5);
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == END_NPC_ID) {
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 1011));
		}
		return HandlerResult.FAILED;
	}
}
