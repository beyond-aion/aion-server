package quest.miragent_holy_templar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Nanou
 */
public class _3935ShoulderTheBurden extends AbstractQuestHandler {

	public _3935ShoulderTheBurden() {
		super(3935);
	}

	@Override
	public void register() {
		int[] npcs = { 203316, 203702, 203329, 203752, 203701 };
		qe.registerQuestNpc(203701).addOnQuestStart(questId);// Lavirintos
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		// 0 - Start to Lavirintos
		if (qs == null || qs.isStartable()) {
			if (targetId == 203701) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {

			switch (targetId) {
				// 1 - Talk with Ettamirel
				case 203316:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				// 2 - Talk with Jupion
				case 203702:
					if (var == 1) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1352);
							case SETPRO2:
								return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				// 3 - Talk with Elizar
				case 203329:
					if (var == 2) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1693);
							case SETPRO3:
								return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					// 4 - Collect Holy Templar Medal and take them to Elizar
					if (var == 3) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2034);
							case CHECK_USER_HAS_QUEST_ITEM:
								if (QuestService.collectItemCheck(env, true)) {
									changeQuestStep(env, 3, 4);
									return sendQuestDialog(env, 10000);
								} else
									return sendQuestDialog(env, 10001);
						}
					}
					break;
				// 5 - Report the result to Jucleas with the Oath Stone
				case 203752:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SET_SUCCEED:
							if (player.getInventory().getItemCountByItemId(186000080) >= 1) {
								removeQuestItem(env, 186000080, 1);
								return defaultCloseDialog(env, 4, 4, true, false);
							} else {
								return sendQuestDialog(env, 2461);
							}
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				// No match
				default:
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203701) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
