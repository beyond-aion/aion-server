package quest.danuar_sanctuary;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pad
 */

public class _16985ChirTreasureRobbers extends QuestHandler {

	private static final int questId = 16985;
	private static final int[] npcIds = { 804864, 804862 };

	public _16985ChirTreasureRobbers() {
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

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == npcIds[0]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcIds[1]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (dialog == DialogAction.SETPRO1)
					return defaultCloseDialog(env, 0, 1);
			} else if (targetId == npcIds[0]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
					return checkQuestItems(env, 1, 1, true, 10002, 10001);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[0])
				return sendQuestEndDialog(env);
		}
		return false;
	}

}
