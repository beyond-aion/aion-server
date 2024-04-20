package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rhys2002, vlog
 */
public class _1156StolenVillageSeal extends AbstractQuestHandler {

	public _1156StolenVillageSeal() {
		super(1156);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203128).addOnQuestStart(questId);
		qe.registerQuestNpc(203128).addOnTalkEvent(questId);
		qe.registerQuestNpc(700003).addOnTalkEvent(questId);
		qe.registerQuestNpc(798003).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203128) { // Santenius
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 700003: // Item Stack
					if (dialogActionId == USE_OBJECT) {
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						}
					} else if (dialogActionId == SETPRO1) {
						changeQuestStep(env, 0, 1); // 1
						return closeDialogWindow(env);
					}
					break;
				case 798003: // Gaphyrk
					if (dialogActionId == QUEST_SELECT) {
						if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
					} else if (dialogActionId == SELECT_QUEST_REWARD) {
						changeQuestStep(env, 1, 1, true); // reward
						return sendQuestDialog(env, 5);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798003) { // Gaphyrk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
