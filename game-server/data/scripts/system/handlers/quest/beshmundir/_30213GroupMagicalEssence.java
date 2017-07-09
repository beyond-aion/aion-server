package quest.beshmundir;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _30213GroupMagicalEssence extends AbstractQuestHandler {

	public _30213GroupMagicalEssence() {
		super(30213);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798941).addOnQuestStart(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
		qe.registerQuestNpc(798926).addOnTalkEvent(questId);
		qe.registerQuestNpc(730275).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798941) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 730275:
					switch (dialogActionId) {
						case SETPRO1: {
							removeQuestItem(env, 182209617, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return true;
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798926) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
