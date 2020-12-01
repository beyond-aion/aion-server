package quest.empyrean_crucible;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Kamui
 */
public class _18209ARiftInTheSpaceTwineContinuum extends AbstractQuestHandler {

	public _18209ARiftInTheSpaceTwineContinuum() {
		super(18209);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205309).addOnQuestStart(questId);
		qe.registerQuestNpc(205309).addOnTalkEvent(questId);
		qe.registerQuestNpc(217819).addOnKillEvent(questId);
		qe.registerQuestNpc(218185).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205309) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205309) {
				switch (env.getDialogActionId()) {
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);

			if (var == 0 && var1 < 4)
				return defaultOnKillEvent(env, 217819, 0, 4, 1);
			else if (var == 0 && var1 == 4)
				return defaultOnKillEvent(env, 217819, 0, 1, 0);
			else if (var == 1 && env.getTargetId() == 218185) {
				qs.setQuestVarById(2, 1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
