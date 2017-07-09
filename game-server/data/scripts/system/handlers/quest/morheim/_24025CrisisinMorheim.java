package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _24025CrisisinMorheim extends AbstractQuestHandler {

	public _24025CrisisinMorheim() {
		super(24025);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204388, 204414, 204304, 204345 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24020);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204388:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 2)
								return sendQuestDialog(env, 1693);
							return false;
						case SETPRO1:
							if (var == 0) {
								return defaultCloseDialog(env, 0, 1); // 1
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 2, 3, false, 10000, 10001);
					}
					break;
				case 204345:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3)
								return sendQuestDialog(env, 2034);
							return false;
						case SET_SUCCEED:
							if (var == 3) {
								return defaultCloseDialog(env, 3, 3, true, false);

							}
					}
					break;
				case 204414:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SELECT2_1_1:
							playQuestMovie(env, 85);
							break;
						case SETPRO2:
							if (var == 1) {
								return defaultCloseDialog(env, 1, 2); // 2
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204304) {
				if (dialogActionId == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else {
					removeQuestItem(env, 182215370, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
