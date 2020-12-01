package quest.fenris_fang;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _4941GroupPandaemoniumHonors extends AbstractQuestHandler {

	public _4941GroupPandaemoniumHonors() {
		super(4941);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204060).addOnQuestStart(questId);
		qe.registerQuestNpc(204060).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204060) {
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
				case 204060:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case CHECK_USER_HAS_QUEST_ITEM:
							long itemCount1 = player.getInventory().getItemCountByItemId(182207121);
							if (itemCount1 >= 1) {
								removeQuestItem(env, 182207121, 1);
								changeQuestStep(env, 1, 1, true);
								return sendQuestDialog(env, 5);
							} else
								return sendQuestDialog(env, 10001);
					}
					break;
				default:
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204060)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
