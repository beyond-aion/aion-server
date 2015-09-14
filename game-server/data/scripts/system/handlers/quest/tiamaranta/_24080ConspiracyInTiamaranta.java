package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Enomine
 */

public class _24080ConspiracyInTiamaranta extends QuestHandler {

	private final static int questId = 24080;
	private final static int[] npc_ids = { 205864, 205964, 205928 };

	public _24080ConspiracyInTiamaranta() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(233868).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 205864:// Skafir
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1011);
						}
						case SETPRO1: {
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 205964:// pashilion
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO2: {
							qs.setQuestVar(2);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 205928:// Hindla
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (player.getInventory().getItemCountByItemId(182215405) == 8) {
								qs.setQuestVar(4);
								updateQuestStatus(env);
								removeQuestItem(env, 182215405, 8);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
						case SETPRO6: {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
			}
		}
		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205864) {// skafir
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 3057);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 233868, 4, 5);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 24071 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

}
