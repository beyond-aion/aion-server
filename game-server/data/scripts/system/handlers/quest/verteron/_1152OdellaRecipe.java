package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Quest starter: Nemia (203132). Give the Odella (182200526) (1) to Eradis (203130) and ask him to cook it. Buy some Verteron Pepper (169400112) for
 * Eradis.
 * 
 * @author vlog
 */
public class _1152OdellaRecipe extends QuestHandler {

	private final static int questId = 1152;

	public _1152OdellaRecipe() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203132).addOnQuestStart(questId);
		qe.registerQuestNpc(203132).addOnTalkEvent(questId);
		qe.registerQuestNpc(203130).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203132) { // Nemia
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182200526, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203130) { // Eradis
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						} else if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1, 0, 0, 182200526, 1); // 1
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 1, true, 5, 2716); // reward
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203130) { // Eradis
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
