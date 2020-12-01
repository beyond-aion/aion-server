package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _11149TheLadyLayout extends AbstractQuestHandler {

	public _11149TheLadyLayout() {
		super(11149);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(296491).addOnQuestStart(questId);
		qe.registerQuestNpc(296491).addOnTalkEvent(questId);
		qe.registerQuestNpc(206155).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206156).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206157).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 296491) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 296491) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(1);
			int var1 = qs.getQuestVarById(2);
			int var2 = qs.getQuestVarById(3);
			if (targetId == 206155 && var == 0) {
				qs.setQuestVarById(1, 1);
				updateQuestStatus(env);
				cheakReward(env);
				return true;
			} else if (targetId == 206156 && var1 == 0) {
				qs.setQuestVarById(2, 1);
				updateQuestStatus(env);
				cheakReward(env);
				return true;
			} else if (targetId == 206157 && var2 == 0) {
				qs.setQuestVarById(3, 1);
				updateQuestStatus(env);
				cheakReward(env);
				return true;
			}
		}
		return false;
	}

	private void cheakReward(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int var = qs.getQuestVarById(1);
		int var1 = qs.getQuestVarById(2);
		int var2 = qs.getQuestVarById(3);
		if (var == 1 && var1 == 1 && var2 == 1) {
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
		}
	}
}
