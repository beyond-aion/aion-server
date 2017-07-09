package quest.miragent_holy_templar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nanou
 */
public class _3934TheQuestForTemplars extends AbstractQuestHandler {

	public _3934TheQuestForTemplars() {
		super(3934);
	}

	@Override
	public void register() {
		int[] npcs = { 798359, 798360, 798361, 798362, 798363, 798364, 798365, 798366, 203752, 203701 };
		qe.registerQuestNpc(203701).addOnQuestStart(questId);// Lavirintos
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

		// 0 - Start to Lavirintos
		if (qs == null || qs.isStartable()) {
			if (targetId == 203701) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env, 182206088, 1);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				// 1 - Talk with Nianalo
				case 798359:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				// 2 - Talk with Navid
				case 798360:
					if (var == 1) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1352);
							case SETPRO2:
								return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				// 3 - Talk with Pavel
				case 798361:
					if (var == 2) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1693);
							case SETPRO3:
								return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					break;
				// 4 - Talk with Pendaon
				case 798362:
					if (var == 3) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2034);
							case SETPRO4:
								return defaultCloseDialog(env, 3, 4); // 4
						}
					}
					break;
				// 5 - Talk with Poevius
				case 798363:
					if (var == 4) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2375);
							case SETPRO5:
								return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				// 6 - Talk with Belicanon
				case 798364:
					if (var == 5) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2716);
							case SETPRO6:
								return defaultCloseDialog(env, 5, 6); // 6
						}
					}
					break;
				// 7 - Talk with Mahelnu
				case 798365:
					if (var == 6) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 3057);
							case SETPRO7:
								return defaultCloseDialog(env, 6, 7); // 7
						}
					}
					break;
				// 8 - Talk with Pater
				case 798366:
					if (var == 7) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 3398);
							case SETPRO8:
								return defaultCloseDialog(env, 7, 8, 182206089, 1, 182206088, 1); // 8
						}
					}
					break;
				// 9 - Report the result to Jucleas with the Oath Stone
				case 203752:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 8) {
								return sendQuestDialog(env, 3739);
							}
							return false;
						case SET_SUCCEED:
							if (player.getInventory().getItemCountByItemId(186000080) >= 1) {
								removeQuestItem(env, 186000080, 1);
								return defaultCloseDialog(env, 8, 8, true, false);
							} else {
								return sendQuestDialog(env, 3825);
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
					return sendQuestEndDialog(env, new int[] { 182206089 });
				}
			}
		}
		return false;
	}
}
