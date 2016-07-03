package quest.rentus_base;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _30550MomentOfCrisis extends QuestHandler {

	private final static int questId = 30550;

	public _30550MomentOfCrisis() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205864).addOnQuestStart(questId); // Skafir
		qe.registerQuestNpc(205864).addOnTalkEvent(questId);
		qe.registerQuestNpc(805156).addOnTalkEvent(questId); // Ekios
		qe.registerQuestNpc(799592).addOnTalkEvent(questId); // Ariana
		qe.registerQuestNpc(799666).addOnTalkEvent(questId); // Ariana
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null || qs == null || qs.isStartable()) {
			if (targetId == 205864) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {

				case 805156: {
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
				}
				case 799592:
				case 799666: {
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD: {
							if (var == 1) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							}
						}
					}
				}
			}

		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 799592:
				case 799666: {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
