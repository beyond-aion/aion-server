package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */
public class _1582ThePriestsNightmare extends AbstractQuestHandler {

	public _1582ThePriestsNightmare() {
		super(1582);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204560).addOnQuestStart(questId);
		qe.registerQuestNpc(204560).addOnTalkEvent(questId);
		qe.registerQuestNpc(700196).addOnTalkEvent(questId);
		qe.registerQuestNpc(204573).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204560) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700196:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (qs.getQuestVarById(0) == 0) {
								qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
								updateQuestStatus(env);
								return true;
							}
						}
					}
					return false;
				case 204560:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SETPRO2: {
							if (qs.getQuestVarById(0) == 1)
								return defaultCloseDialog(env, 1, 2);
						}
					}
					return false;
				case 204573:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (qs.getQuestVarById(0) == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SETPRO3:
							if (qs.getQuestVarById(0) == 2) {
								qs.setRewardGroup(0); // reward green resurrection stone
								return defaultCloseDialog(env, 2, 2, true, false);
							}
							return false;
						case SETPRO4:
							if (qs.getQuestVarById(0) == 2) {
								qs.setRewardGroup(1); // reward white resurrection stone
								return defaultCloseDialog(env, 2, 2, true, false);
							}
							return false;
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 700196) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
