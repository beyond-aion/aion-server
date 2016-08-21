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
public class _28910AStabbingInSauro extends QuestHandler {

	private final static int questId = 28910;

	public _28910AStabbingInSauro() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801946).addOnQuestStart(questId); // Sibeldum.
		qe.registerQuestNpc(801947).addOnTalkEvent(questId); // Giriltia.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801946) { // Sibeldum.
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
				case 801947: { // Giriltia.
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
			if (targetId == 801947) { // Giriltia.
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
