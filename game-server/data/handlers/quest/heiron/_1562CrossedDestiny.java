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
 * Find Litonos (204616) (bring him the Berone's Necklace (182201780)). Talk with Litonos. Take Litonos to Berone (204589). Talk with Berone.
 * 
 * @author Balthazar, vlog
 */
public class _1562CrossedDestiny extends AbstractQuestHandler {

	public _1562CrossedDestiny() {
		super(1562);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204589).addOnQuestStart(questId);
		qe.registerOnLogOut(questId);
		qe.registerQuestNpc(204589).addOnTalkEvent(questId);
		qe.registerQuestNpc(204616).addOnTalkEvent(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204589) { // Berone
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				if (env.getDialogActionId() == ASK_QUEST_ACCEPT)
					return sendQuestDialog(env, 4);
				if (env.getDialogActionId() == QUEST_REFUSE_1)
					return sendQuestDialog(env, 1004);
				if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return defaultCloseDialog(env, 0, 1, false, false, 182201780, 1, 0, 0);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204616: // Litonos
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (qs.getQuestVarById(0) == 1 && player.getInventory().getItemCountByItemId(182201780) == 1)
								return sendQuestDialog(env, 1352);
							else
								return sendQuestDialog(env, 1438);
						case FINISH_DIALOG:
							return defaultCloseDialog(env, 0, 0);
						case SETPRO2:
							if (qs.getQuestVarById(0) == 1) {
								defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 204589, 0, 0);
								return defaultCloseDialog(env, 1, 2, false, false, 0, 0, 182201780, 1); // 2
							}
							return false;
						case USE_OBJECT:
							if (qs.getQuestVarById(0) == 1) {
								return defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 204589, 1, 2);
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204589) { // Berone
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				changeQuestStep(env, 2, 1);
			}
		}
		return false;
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 2, true); // reward
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 1, false); // 1
	}
}
