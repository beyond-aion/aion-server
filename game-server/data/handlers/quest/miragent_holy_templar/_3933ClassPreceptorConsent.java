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
public class _3933ClassPreceptorConsent extends AbstractQuestHandler {

	public _3933ClassPreceptorConsent() {
		super(3933);
	}

	@Override
	public void register() {
		int[] npcs = { 203704, 203705, 203706, 203707, 203752, 203701, 801214, 801215 };
		qe.registerQuestNpc(203701).addOnQuestStart(questId);// Lavirintos
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
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
					return sendQuestStartDialog(env, 182206086, 1);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203704: // 1 - Receive the signature of Boreas on the recommendation letter.
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 203705: // 2 - Receive the signature of Jumentis on the recommendation letter.
					if (var == 1) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1352);
							case SETPRO2:
								return defaultCloseDialog(env, 1, 2);
						}
					}
					break;
				case 203706: // 3 - Receive the signature of Charna on the recommendation letter.
					if (var == 2) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1693);
							case SETPRO3:
								return defaultCloseDialog(env, 2, 3);
						}
					}
					break;
				case 203707: // 4 - Receive the signature of Thrasymedes on the recommendation letter.
					if (var == 3) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2034);
							case SETPRO4:
								return defaultCloseDialog(env, 3, 4);
						}
					}
					break;
				case 801214: // 5 - Receive the signature of Oakley on the recommendation letter.
					if (var == 4) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2375);
							case SETPRO5:
								return defaultCloseDialog(env, 4, 5);
						}
					}
					break;
				case 801215: // 6 - Receive the signature of Dion on the recommendation letter.
					if (var == 5) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2716);
							case SETPRO6:
								return defaultCloseDialog(env, 5, 6, 182206087, 1, 182206086, 1);
						}
					}
					break;
				case 203752: // 7 - Report the result to Lavirintos with the Oath Stone
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SET_SUCCEED:
							if (player.getInventory().getItemCountByItemId(186000080) >= 1) {
								removeQuestItem(env, 186000080, 1);
								return defaultCloseDialog(env, 6, 6, true, false);
							} else {
								return sendQuestDialog(env, 3143);
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
					return sendQuestEndDialog(env, new int[] { 182206087 });
				}
			}
		}
		return false;
	}
}
