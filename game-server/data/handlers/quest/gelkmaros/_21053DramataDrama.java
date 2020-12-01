package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _21053DramataDrama extends AbstractQuestHandler {

	public _21053DramataDrama() {
		super(21053);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799320).addOnQuestStart(questId);
		qe.registerQuestNpc(799320).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799320) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SELECT1_1:
						return sendQuestDialog(env, 1012);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_ACCEPT_1:
						return sendQuestStartDialog(env);
					case QUEST_REFUSE_1:
						return sendQuestDialog(env, 1004);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799320) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT4:
						return sendQuestDialog(env, 2034);
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, var, var, true, 5, 2716);
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);

				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799320) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
