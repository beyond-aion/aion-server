package quest.raksang_ruins;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pad
 */
public class _28742AFlurryofActivity extends QuestHandler {

	private static final int questId = 28742;
	private static final int itemId = 182215697;
	//TODO: fix quest start, 804717 Proqura is working
	private static final int[] npcIds = { 804717, 804732 };

	public _28742AFlurryofActivity() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcIds[0]).addOnQuestStart(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == npcIds[0]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == npcIds[1]) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0)
							return sendQuestDialog(env, 1011);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						if (player.getInventory().getItemCountByItemId(itemId) < 1) {
							return sendQuestDialog(env, 10001);
						}
						else {
							removeQuestItem(env, itemId, 1);
							qs.setQuestVarById(0, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 10000);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[1]) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

}
