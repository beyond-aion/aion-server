package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _1336ScoutingForDemokritos extends AbstractQuestHandler {

	public _1336ScoutingForDemokritos() {
		super(1336);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204006).addOnQuestStart(questId); // Demokritos
		qe.registerQuestNpc(204006).addOnTalkEvent(questId);
		qe.registerQuestNpc(206020).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206021).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(206022).addOnAtDistanceEvent(questId);
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
			if (targetId == 204006) { // Demokritos
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204006) {
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
			if (targetId == 206020 && var == 0) {
				playQuestMovie(env, 43);
				changeQuestStep(env, 0, 16);
				return true;
			} else if (targetId == 206021 && var == 16) {
				playQuestMovie(env, 44);
				changeQuestStep(env, 16, 48);
				return true;
			} else if (targetId == 206022 && var == 48) {
				playQuestMovie(env, 45);
				changeQuestStep(env, 48, 48, true);
				return true;
			}
		}
		return false;
	}
}
