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
public class _28738BombsforEveryone extends QuestHandler {

	private static final int questId = 28738, skillId = 10981;
	private static final int[] npcIds = { 206395, 206396, 206397, 804966, 702694 };

	public _28738BombsforEveryone() {
		super(questId);
	}

	@Override
	public void register() {
		for (int i = 0; i <= 2; i++) {
			qe.registerQuestNpc(npcIds[i]).addOnQuestStart(questId);
			qe.registerQuestNpc(npcIds[i]).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(npcIds[3]).addOnTalkEvent(questId);
		qe.registerQuestNpc(npcIds[4]).addOnTalkEvent(questId);
		qe.registerQuestSkill(skillId, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == npcIds[0] || targetId == npcIds[1] || targetId == npcIds[2]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			/*
			 * added at 4.9
			 * if (targetId == npcIds[3]) {
			 * switch (dialog) {
			 * case QUEST_SELECT:
			 * if (qs.getQuestVarById(0) == 1)
			 * return sendQuestDialog(env, 10002);
			 * break;
			 * case SELECT_QUEST_REWARD:
			 * return defaultCloseDialog(env, 0, 0, true, true);
			 * }
			 * } else
			 */
			if (targetId == npcIds[4]) {
				if (dialog == DialogAction.USE_OBJECT)
					return giveQuestItem(env, 164000342, 10);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[3]) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var0 = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);
			if (var0 == 0 && skillId == _28738BombsforEveryone.skillId) {
				if (var1 < 9) {
					qs.setQuestVarById(1, var1 + 1);
				} else if (var1 == 9) {
					qs.setQuestVarById(0, 1);
					qs.setStatus(QuestStatus.REWARD); // remove at 4.9
				}
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
