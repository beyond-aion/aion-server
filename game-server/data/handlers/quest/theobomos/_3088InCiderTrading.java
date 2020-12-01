package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _3088InCiderTrading extends AbstractQuestHandler {

	public _3088InCiderTrading() {
		super(3088);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798202).addOnQuestStart(questId);
		qe.registerQuestNpc(798202).addOnTalkEvent(questId);
		qe.registerQuestNpc(798201).addOnTalkEvent(questId);
		qe.registerQuestNpc(798204).addOnTalkEvent(questId);
		qe.registerQuestNpc(798132).addOnTalkEvent(questId);
		qe.registerQuestNpc(798166).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798202) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798202) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						if (qs.getRewardGroup() == null)
							return sendQuestDialog(env, 1011);
						else if (qs.getRewardGroup() == 0)
							return sendQuestDialog(env, 1352);
						else if (qs.getRewardGroup() == 1)
							return sendQuestDialog(env, 2034);
						else if (qs.getRewardGroup() == 2)
							return sendQuestDialog(env, 3398);
					}
				} else if (dialogActionId == SELECT1_1) {
					if (player.getInventory().getItemCountByItemId(160003020) >= 1)
						return sendQuestDialog(env, 1012);
					else
						return sendQuestDialog(env, 1267);
				} else if (dialogActionId == SELECT1_2) {
					if (player.getInventory().getItemCountByItemId(160003020) >= 10)
						return sendQuestDialog(env, 1097);
					else
						return sendQuestDialog(env, 1267);
				} else if (dialogActionId == SELECT1_3) {
					if (player.getInventory().getItemCountByItemId(160003020) >= 100)
						return sendQuestDialog(env, 1182);
					else
						return sendQuestDialog(env, 1267);
				} else if (dialogActionId == SELECT1) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == SETPRO1) {
					qs.setRewardGroup(0);
					player.getInventory().decreaseByItemId(160003020, 1);
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 0, 2, workItems.get(0).getItemId(), workItems.get(0).getCount());
				} else if (dialogActionId == SETPRO3) {
					qs.setRewardGroup(1);
					player.getInventory().decreaseByItemId(160003020, 10);
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					return defaultCloseDialog(env, 0, 4);
				} else if (dialogActionId == SETPRO7) {
					qs.setRewardGroup(2);
					player.getInventory().decreaseByItemId(160003020, 100);
					return sendQuestDialog(env, 3398);
				} else if (dialogActionId == SETPRO8) {
					return defaultCloseDialog(env, 0, 8);
				}
			}
			if (targetId == 798201) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2) {
						return sendQuestDialog(env, 1693);
					}
					if (qs.getQuestVarById(0) == 5) {
						return sendQuestDialog(env, 2716);
					}
				} else if (dialogActionId == SETPRO6) {
					return defaultCloseDialog(env, 5, 6);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
			if (targetId == 798204) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 4) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == SETPRO5) {
					return defaultCloseDialog(env, 4, 5);
				}
			}
			if (targetId == 798132) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 6) {
						return sendQuestDialog(env, 3057);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 6, 6, true, true);
				}
			}
			if (targetId == 798166) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 8) {
						if (player.getInventory().getItemCountByItemId(182208064) >= 1)
							return sendQuestDialog(env, 3739);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					player.getInventory().decreaseByItemId(182208064, 1);
					return defaultCloseDialog(env, 8, 8, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798201 && qs.getQuestVarById(0) == 2 || targetId == 798132 && qs.getQuestVarById(0) == 6
				|| targetId == 798166 && qs.getQuestVarById(0) == 8) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
