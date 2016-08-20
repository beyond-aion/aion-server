package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 */
public class _10033PetrifiedSubside extends QuestHandler {

	public _10033PetrifiedSubside() {
		super(10033);
	}

	@Override
	public void register() {
		// Pomponia ID: 798970
		// Sulla ID: 798975
		// Philon ID: 798981
		// Western Petrified Mass ID: 730226
		// Eastern Petrified Mass ID: 730227
		// Southern Petrified Mass ID: 730228
		int[] npcs = { 798970, 798975, 798981, 730226, 730227, 730228 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		DialogAction dialog = env.getDialog();
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798970: // Pomponia
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
					break;
				case 798975: // Sulla
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							break;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
						case SET_SUCCEED:
							if (var == 6) {
								removeQuestItem(env, 182215625, 1);
								qs.setQuestVar(var + 1); // 11
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return closeDialogWindow(env);
							}
					}
					break;
				case 798981: // Philon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							giveQuestItem(env, 182215622, 1);
							return defaultCloseDialog(env, 2, 3);
						}
					}
					break;
				case 730226: // Western Petrified Mass
					if (var == 3) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}

						if (dialog == DialogAction.SETPRO4) {
							removeQuestItem(env, 182215622, 1);
							giveQuestItem(env, 182215623, 1);
							return defaultCloseDialog(env, var, var + 1);
						}
					}
					break;
				case 730227: // Eastern Petrified Mass
					if (var == 4) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}

						if (dialog == DialogAction.SETPRO5) {
							removeQuestItem(env, 182215623, 1);
							giveQuestItem(env, 182215624, 1);
							return defaultCloseDialog(env, var, var + 1);
						}
					}
					break;
				case 730228: // Southern Petrified Mass
					if (var == 5) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2716);
						}

						if (dialog == DialogAction.SETPRO6) {
							removeQuestItem(env, 182215624, 1);
							giveQuestItem(env, 182215625, 1);
							return defaultCloseDialog(env, var, var + 1);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798970) { // Pomponia
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10031);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10031);
	}
}
