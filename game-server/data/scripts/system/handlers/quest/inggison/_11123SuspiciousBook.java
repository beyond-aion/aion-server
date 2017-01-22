package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _11123SuspiciousBook extends QuestHandler {

	private final static int questId = 11123;

	public _11123SuspiciousBook() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182206798, questId);
		qe.registerQuestNpc(798991).addOnTalkEvent(questId);
		qe.registerQuestNpc(798947).addOnTalkEvent(questId);
		qe.registerQuestNpc(700616).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == 700616) {
				return giveQuestItem(env, 182206798, 1);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798991) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO1) {
					qs.setQuestVar(1);
					return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798947) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182206798, 1);
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
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
