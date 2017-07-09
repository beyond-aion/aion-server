package quest.sauro_supply_base;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Tibald
 */
public class _18910TheSauroSupplyBase extends AbstractQuestHandler {

	public _18910TheSauroSupplyBase() {
		super(18910);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801944).addOnQuestStart(questId); // Amalde.
		qe.registerQuestNpc(801945).addOnTalkEvent(questId); // Kanix.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 801944) { // Amalde.
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 801945: { // Kanix.
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							changeQuestStep(env, 0, 0, true);
							return sendQuestEndDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801945) { // Kanix.
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
