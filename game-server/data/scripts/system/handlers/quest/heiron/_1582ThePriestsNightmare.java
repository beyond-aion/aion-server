package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */

public class _1582ThePriestsNightmare extends QuestHandler {

	private final static int questId = 1582;

	public _1582ThePriestsNightmare() {
		super(questId);
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

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204560) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700196: {
					switch (env.getDialog()) {
						case USE_OBJECT: {
							if (qs.getQuestVarById(0) == 0) {
								qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
								updateQuestStatus(env);
								return true;
							}
						}
					}
				}
				case 204560: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							if (qs.getQuestVarById(0) == 1) {
								qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10);
							}
						}
					}
				}
				case 204573: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							if (qs.getQuestVarById(0) == 2) {
								qs.setQuestVar(2);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10);
							}
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 700196) {
				if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
