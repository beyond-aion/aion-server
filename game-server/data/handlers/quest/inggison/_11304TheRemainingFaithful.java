package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author bobobear Talk to Pilomenes. Kill Isbariya the Resolute at Beshmundir Temple and find the Balaur orders. Afterwards, take it to Outremus.
 */
public class _11304TheRemainingFaithful extends AbstractQuestHandler {

	public _11304TheRemainingFaithful() {
		super(11304);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798927).addOnQuestStart(questId);
		qe.registerQuestNpc(798927).addOnTalkEvent(questId);
		qe.registerQuestNpc(798926).addOnTalkEvent(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
		qe.registerOnGetItem(182215336, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798927) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798941:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798926)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			changeQuestStep(env, 1, 2, true); // reward
			return true;
		}
		return false;
	}
}
