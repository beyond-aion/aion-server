package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Artur, Majka
 */
public class _14051RootOfTheProblem extends AbstractQuestHandler {

	public _14051RootOfTheProblem() {
		super(14051);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204500, 204549, 730026, 730024 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestItem(182215337, questId);
		qe.registerQuestItem(182215338, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14050);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14050);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) { // Trajanus
			if (targetId == 730024) {
				removeQuestItem(env, 182215337, 3);
				removeQuestItem(env, 182215338, 3);
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204500) { // Perento
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					return false;
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1); // 1
			}
		} else if (targetId == 204549) { // Aphesius
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
					return false;
				case SELECT2_1:
					return sendQuestDialog(env, 1353);
				case SELECT2_1_1:
					return sendQuestDialog(env, 1354);
				case SETPRO2:
					return defaultCloseDialog(env, 1, 2); // 2
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						changeQuestStep(env, 2, 3);
						updateQuestStatus(env);
						return sendQuestDialog(env, 10000); // 3
					} else
						return sendQuestDialog(env, 10001);
			}
		} else if (targetId == 730026) {// Mersephon
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2034);
					return false;
				case SELECT4_1:
					return sendQuestDialog(env, 2035);
				case SETPRO4:
					return defaultCloseDialog(env, 3, 4, true, false); // 4
			}
		}
		return false;
	}
}
