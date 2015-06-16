package quest.beshmundir;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */

public class _30210WritteninBlood extends QuestHandler {

	private final static int questId = 30210;

	public _30210WritteninBlood() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203837).addOnQuestStart(questId);
		qe.registerQuestNpc(203837).addOnTalkEvent(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203837) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}
		
		if (qs == null)
			return false;

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203837) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					if (var == 0 && player.getInventory().getItemCountByItemId(182209613) >= 30) {
						removeQuestItem(env, 182209613, 30);
						changeQuestStep(env, 0, 0, true, 0);
						return sendQuestDialog(env, 10000);
					}
					else
						return sendQuestDialog(env, 10001);
				}
				return false;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798941) {
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
