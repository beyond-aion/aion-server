package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author cheatkiller
 */
public class _13056DonRockTheParty extends QuestHandler {

	private final static int questId = 13056;

	public _13056DonRockTheParty() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 801094, 701694 };
		qe.registerQuestNpc(801094).addOnQuestStart(questId);
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801094) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 801094) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 0, 1, false, 10000, 10001, 182213480, 1);
					}
				}
			} else if (targetId == 701694) {
				if (var < 3)
					return useQuestObject(env, var, var + 1, false, true);
				else {
					return useQuestObject(env, 3, 4, true, true);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801094) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					removeQuestItem(env, 182213480, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
