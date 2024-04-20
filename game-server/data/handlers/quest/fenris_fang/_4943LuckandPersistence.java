package quest.fenris_fang;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Nanou, Gigi
 */
public class _4943LuckandPersistence extends AbstractQuestHandler {

	public _4943LuckandPersistence() {
		super(4943);
	}

	@Override
	public void register() {
		int[] npcs = { 204096, 204097, 204075, 204053, 700538 };
		qe.registerQuestNpc(204053).addOnQuestStart(questId); // Kvasir
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		// 0 - Start to Kvasir
		if (qs == null || qs.isStartable()) {
			if (targetId == 204053) {
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
				// 1 - Talk with Latatusk
				case 204096:
					if (var == 0) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1011);
							case SETPRO1:
								return defaultCloseDialog(env, 0, 1);
						}
					}
					if (var == 2) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1693);
							case CHECK_USER_HAS_QUEST_ITEM:
								if (QuestService.collectItemCheck(env, true)) {
									removeQuestItem(env, 122001275, 1);
									changeQuestStep(env, 2, 3);
									return sendQuestDialog(env, 10000);
								} else
									return sendQuestDialog(env, 10001);
						}
					}
					break;
				// 2 - Talk with Relir.
				case 204097:
					if (var == 1) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 1352);
							case SELECT2_1_1:
								if (player.getInventory().tryDecreaseKinah(3400000)) {
									if (player.getInventory().getItemCountByItemId(122001275) == 0) {
										if (!giveQuestItem(env, 122001275, 1))
											return true;
									}
									changeQuestStep(env, 1, 2);
									return sendQuestDialog(env, 1354);
								} else {
									return sendQuestDialog(env, 1438);
								}
						}
					}
					break;
				case 700538:
					if (dialogActionId == USE_OBJECT && var == 2) {
						return useQuestObject(env, 2, 2, false, 0);
					}
					break;
				// 4 - Better purify yourself! Take Glossy Holy Water and visit High Priest Balder
				case 204075:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SET_SUCCEED:
							if (player.getInventory().getItemCountByItemId(186000084) >= 1) {
								removeQuestItem(env, 186000084, 1);
								return defaultCloseDialog(env, 3, 3, true, false);
							} else {
								return sendQuestDialog(env, 2120);
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
			// 5 - Talk with Kvasir
			if (targetId == 204053) {
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
