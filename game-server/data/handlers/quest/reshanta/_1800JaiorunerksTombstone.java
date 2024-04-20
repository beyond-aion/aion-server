package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi, vlog
 */
public class _1800JaiorunerksTombstone extends AbstractQuestHandler {

	public _1800JaiorunerksTombstone() {
		super(1800);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279016).addOnQuestStart(questId);
		qe.registerQuestNpc(279016).addOnTalkEvent(questId);
		qe.registerQuestNpc(730141).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 279016) { // Vindachinerk
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182202163, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 730141: { // Jaiorunerk's Tomb
					switch (dialogActionId) {
						case USE_OBJECT:
							if (var == 0) {
								if (player.getInventory().getItemCountByItemId(182202163) > 0) {
									return sendQuestDialog(env, 1352);
								}
							}
							return false;
						case SETPRO1:
							removeQuestItem(env, 182202163, 1);
							changeQuestStep(env, 0, 1);
							return closeDialogWindow(env);
					}
					break;
				}
				case 279016: // Vindachinerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SELECT_QUEST_REWARD:
							changeQuestStep(env, 1, 1, true); // reward
							return sendQuestDialog(env, 5);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279016) { // Vindachinerk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
