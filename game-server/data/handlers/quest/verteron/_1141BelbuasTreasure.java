package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke, Dune11, vlog
 */
public class _1141BelbuasTreasure extends AbstractQuestHandler {

	public _1141BelbuasTreasure() {
		super(1141);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(730001).addOnQuestStart(questId);
		qe.registerQuestNpc(730001).addOnTalkEvent(questId);
		qe.registerQuestNpc(700122).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 730001) { // Nola
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 700122) { // Belbua's Wine Barrel
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					changeQuestStep(env, 0, 0, true); // reward
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 700122) { // Belbua's Wine Barrel
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
