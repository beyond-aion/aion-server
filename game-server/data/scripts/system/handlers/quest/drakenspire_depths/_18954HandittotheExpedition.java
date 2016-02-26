package quest.drakenspire_depths;

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
 * @author Pad
 */
public class _18954HandittotheExpedition extends QuestHandler {

	private static final int questId = 18954;
	private static final int itemId = 182215754;
	private static final int npcId = 804711;

	public _18954HandittotheExpedition() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(itemId, questId);
		qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (dialog == DialogAction.QUEST_ACCEPT_1) {
				QuestService.startQuest(env);
				return closeDialogWindow(env);
			} else if (dialog == DialogAction.QUEST_REFUSE_1)
				return closeDialogWindow(env);
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcId) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (dialog == DialogAction.SELECT_QUEST_REWARD)
					return defaultCloseDialog(env, 0, 0, true, true);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcId) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 5);
				} else {
					removeQuestItem(env, itemId, 1);
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

		if (item.getItemId() != itemId)
			return HandlerResult.FAILED;

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
