package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke, Nephis and quest helper team, vlog
 */
public class _2232TheBrokenHoneyJar extends AbstractQuestHandler {

	public _2232TheBrokenHoneyJar() {
		super(2232);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203613).addOnQuestStart(questId);
		qe.registerQuestNpc(203613).addOnTalkEvent(questId);
		qe.registerQuestNpc(203622).addOnTalkEvent(questId);
		qe.registerQuestNpc(700061).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203613) { // Gilungk
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203613) { // Gilungk
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 1, true, 5, 2716); // reward
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
				}
			} else if (targetId == 700061) { // Beehive
				if (dialogActionId == USE_OBJECT) {
					return true; // loot
				}
			} else if (targetId == 203622) { // Tatural
				if (dialogActionId == QUEST_SELECT) {
					if (var == 0) {
						return sendQuestDialog(env, 1352);
					}
				} else if (env.getDialogActionId() == SETPRO1) {
					return defaultCloseDialog(env, 0, 1); // 1
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203613) { // Gilungk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
