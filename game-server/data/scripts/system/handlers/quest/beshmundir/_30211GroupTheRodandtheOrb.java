package quest.beshmundir;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */

public class _30211GroupTheRodandtheOrb extends QuestHandler {

	private final static int questId = 30211;

	public _30211GroupTheRodandtheOrb() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798941).addOnQuestStart(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
		qe.registerQuestNpc(730275).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
			
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798941) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 730275: {
					switch (dialog) {
						case SETPRO1: {
							removeQuestItem(env, 182209614, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return true;
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798941) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
