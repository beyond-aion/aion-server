package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _21052DragonHuntin extends QuestHandler {

	private final static int questId = 21052;

	public _21052DragonHuntin() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799268).addOnQuestStart(questId);
		qe.registerQuestNpc(799268).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799268) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SELECT_ACTION_1012:
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
			if (targetId == 799268) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_ACTION_2034:
						return sendQuestDialog(env, 2034);
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, var, var, true, 5, 2716);
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);

				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799268) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
