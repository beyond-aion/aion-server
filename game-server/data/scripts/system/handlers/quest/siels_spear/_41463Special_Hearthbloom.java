package quest.siels_spear;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _41463Special_Hearthbloom extends QuestHandler {

	private final static int questId = 41463;

	public _41463Special_Hearthbloom() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205579).addOnQuestStart(questId);
		qe.registerQuestNpc(205579).addOnTalkEvent(questId);
		qe.registerQuestNpc(205580).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205579) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
					case QUEST_REFUSE_SIMPLE:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 205580: {
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							} else if (player.getInventory().getItemCountByItemId(182213224) >= 7) {
								return sendQuestDialog(env, 2375);
							} else
								return closeDialogWindow(env);
						}
						case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: {
							removeQuestItem(env, 182213224, 7);
							qs.setQuestVar(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						}
						case SETPRO1: {
							if (giveQuestItem(env, 170190066, 1))
								return defaultCloseDialog(env, 0, 1);
						}
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205580)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
