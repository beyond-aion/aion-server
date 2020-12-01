package quest.talocs_hollow;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _21468TheStruggleWithin extends AbstractQuestHandler {

	public _21468TheStruggleWithin() {
		super(21468);
	}

	@Override
	public void register() {
		qe.registerQuestSkill(9832, questId);
		qe.registerQuestSkill(9833, questId);
		qe.registerQuestSkill(9834, questId);
		qe.registerQuestNpc(799526).addOnQuestStart(questId);
		qe.registerQuestNpc(799503).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799526) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799503) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var1 = qs.getQuestVarById(1);
			int var2 = qs.getQuestVarById(2);
			int var3 = qs.getQuestVarById(3);
			if (skillUsedId == 9832) {
				if (var1 < 10) {
					qs.setQuestVarById(1, var1 + 1);
					updateQuestStatus(env);
					reward(qs, env);
				}
			} else if (skillUsedId == 9833) {
				if (var2 < 5) {
					qs.setQuestVarById(2, var2 + 1);
					updateQuestStatus(env);
					reward(qs, env);
				}
			} else if (skillUsedId == 9834) {
				if (var3 < 3) {
					qs.setQuestVarById(3, var3 + 1);
					updateQuestStatus(env);
					reward(qs, env);
				}
			}
		}
		return false;
	}

	private void reward(QuestState qs, QuestEnv env) {
		if (qs.getQuestVarById(1) == 10 && qs.getQuestVarById(2) == 5 && qs.getQuestVarById(3) == 3) {
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
		}
	}
}
