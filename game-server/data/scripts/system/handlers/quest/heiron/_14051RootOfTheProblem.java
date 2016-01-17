package quest.heiron;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Artur
 * @Modified Majka
 */
public class _14051RootOfTheProblem extends QuestHandler {

	private final static int questId = 14051;
	private final static int[] npc_ids = { 204500, 204549, 730026, 730024 };

	public _14051RootOfTheProblem() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182215337, questId);
		qe.registerQuestItem(182215338, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14050);
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

		if (qs.getStatus() == QuestStatus.REWARD) { //Trajanus
			if (targetId == 730024){
				removeQuestItem(env, 182215337, 3);
				removeQuestItem(env, 182215338, 3);
			return sendQuestEndDialog(env);
		}
		}
		else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204500) { //Perento
			switch (env.getDialog()) {
				case QUEST_SELECT: 
					if (var == 0)
						return sendQuestDialog(env, 1011);
				case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
			}
		}
		else if (targetId == 204549) { //Aphesius
			switch (env.getDialog()) {
				case QUEST_SELECT: {
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
				}
				case SELECT_ACTION_1353: 
					return sendQuestDialog(env, 1353);
				case SELECT_ACTION_1354:
					return sendQuestDialog(env, 1354);
				case SETPRO2: 
					return defaultCloseDialog(env, 1, 2); // 2
				case CHECK_USER_HAS_QUEST_ITEM: 
					if (QuestService.collectItemCheck(env, true)) {
						changeQuestStep(env, 2, 3, false);
						updateQuestStatus(env);
						return sendQuestDialog(env, 10000);  //3
					}
					else
						return sendQuestDialog(env, 10001);
			}
		}
		else if (targetId == 730026) {//Mersephon
			switch (env.getDialog()) {
				case QUEST_SELECT: {
					if (var == 3)
						return sendQuestDialog(env, 2034);
				}
				case SELECT_ACTION_2035:
					return sendQuestDialog(env, 2035);					
				case SETPRO4: 
					return defaultCloseDialog(env, 3, 4, true, false); // 4
				}
		}
		return false;
	}
}
