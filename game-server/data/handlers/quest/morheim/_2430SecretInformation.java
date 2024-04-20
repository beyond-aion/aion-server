package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author MrPoke, Nephis, vlog
 */
public class _2430SecretInformation extends AbstractQuestHandler {

	public _2430SecretInformation() {
		super(2430);
	}

	@Override
	public void register() {
		int[] npcs = { 204327, 204377, 205244, 798081, 798082, 204300 };
		qe.registerQuestNpc(204327).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204327) { // Sveinn
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_REFUSE_1:
						return sendQuestDialog(env, 1004);
					case QUEST_ACCEPT_1:
						return sendQuestDialog(env, 1003);
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
					case SETPRO1:
						if (player.getInventory().getKinah() >= 500) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(500);
								changeQuestStep(env, 0, 1); // 1
								return sendQuestDialog(env, 1352);
							}
						} else {
							return sendQuestDialog(env, 1267);
						}
						return false;
					case SETPRO3:
						if (player.getInventory().getKinah() >= 5000) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(5000);
								changeQuestStep(env, 0, 3); // 3
								return sendQuestDialog(env, 2034);
							}
						} else {
							return sendQuestDialog(env, 1267);
						}
						return false;
					case SETPRO7:
						if (player.getInventory().getKinah() >= 50000) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(50000);
								changeQuestStep(env, 0, 7); // 7
								return sendQuestDialog(env, 3398);
							}
						} else {
							return sendQuestDialog(env, 1267);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204327: // Sveinn
					switch (dialogActionId) {
						case QUEST_SELECT:
							switch (var) {
								case 1: {
									return sendQuestDialog(env, 1352);
								}
								case 3: {
									return sendQuestDialog(env, 2034);
								}
								case 7: {
									return sendQuestDialog(env, 3398);
								}
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2, 182204221, 1, 0, 0); // 2
						case SETPRO4:
							return defaultCloseDialog(env, 3, 4); // 4
						case SETPRO8:
							return defaultCloseDialog(env, 7, 8); // 8
					}
					break;
				case 204377: // Grall
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SELECT_QUEST_REWARD:
							removeQuestItem(env, 182204221, 1);
							changeQuestStep(env, 2, 2, true);
							qs.setRewardGroup(0);
							return sendQuestEndDialog(env);
					}
					break;
				case 205244: // Hugorunerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SETPRO5:
							return defaultCloseDialog(env, 4, 5); // 5
					}
					break;
				case 798081: // Nicoyerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
							return false;
						case SETPRO6:
							return defaultCloseDialog(env, 5, 6); // 6
					}
					break;
				case 798082: // Bicorunerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SELECT_QUEST_REWARD:
							changeQuestStep(env, 6, 6, true);
							qs.setRewardGroup(1);
							return sendQuestEndDialog(env);
					}
					break;
				case 204300: // Bolverk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 8) {
								if (player.getInventory().getItemCountByItemId(182204222) > 0) {
									return sendQuestDialog(env, 3739);
								} else {
									return sendQuestDialog(env, 3825);
								}
							}
							return false;
						case SELECT_QUEST_REWARD:
							removeQuestItem(env, 182204222, 1);
							changeQuestStep(env, 8, 8, true);
							qs.setRewardGroup(2);
							return sendQuestEndDialog(env);
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204377: // Grall
					if (var == 2)
						return sendQuestEndDialog(env);
					break;
				case 798082: // Bicorunerk
					if (var == 6)
						return sendQuestEndDialog(env);
					break;
				case 204300: // Bolverk
					if (var == 8)
						return sendQuestEndDialog(env);
					break;
			}
		}
		return false;
	}
}
