package quest.heiron;

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
public class _1661FindingTheForges extends AbstractQuestHandler {

	public _1661FindingTheForges() {
		super(1661);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204600).addOnQuestStart(questId);
		qe.registerQuestNpc(204600).addOnTalkEvent(questId);
		qe.registerQuestNpc(206045).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206046).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206047).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204600) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == QUEST_ACCEPT_1) {
					playQuestMovie(env, 200);
					return sendQuestStartDialog(env);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204600) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else
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
			int var = qs.getQuestVarById(0);
			if (targetId == 206045 && var == 0) {
				changeQuestStep(env, 0, 16);
				return true;
			} else if (targetId == 206046 && var == 16) {
				changeQuestStep(env, 16, 48);
				return true;
			} else if (targetId == 206047 && var == 48) {
				changeQuestStep(env, 48, 48, true);
				return true;
			}
		}
		return false;
	}
}
