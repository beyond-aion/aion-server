package quest.sauro_supply_base;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Tibald
 */
public class _18910TheSauroSupplyBase extends QuestHandler {

	private final static int questId = 18910;

	public _18910TheSauroSupplyBase() {
		super(questId);
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
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 801944) { // Amalde.
				switch (dialog) {
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
					switch (dialog) {
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
