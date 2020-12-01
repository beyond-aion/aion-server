package quest.chantra_dredgion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _4725CeaselessAttack extends AbstractQuestHandler {

	public _4725CeaselessAttack() {
		super(4725);
	}

	@Override
	public void register() {
		qe.registerOnDredgionReward(questId);
		qe.registerQuestNpc(799403).addOnQuestStart(questId);
		qe.registerQuestNpc(799403).addOnTalkEvent(questId);
		qe.registerQuestNpc(799226).addOnTalkEvent(questId);
		qe.registerQuestNpc(281866).addOnKillEvent(questId);
		qe.registerQuestNpc(216866).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799403) { // Yorgen
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var1 = qs.getQuestVarById(1);
			int var2 = qs.getQuestVarById(2);
			if (targetId == 799226) { // Valetta
				if (dialogActionId == QUEST_SELECT) {
					if (var1 == 6 && var2 == 15) {
						return sendQuestDialog(env, 10002);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799226) { // Valetta
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int[] mobs = { 281866, 216866 };
		return defaultOnKillEvent(env, mobs, 0, 15, 2); // 2: 0 - 15
	}

	@Override
	public boolean onDredgionRewardEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var1 = qs.getQuestVarById(1);
			if (var1 < 6) {
				changeQuestStep(env, var1, var1 + 1, false, 1); // 1: 0 - 6
				return true;
			}
		}
		return false;
	}
}
