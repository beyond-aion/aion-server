package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _22504TooLittleButterOverTooMuchBread extends QuestHandler {

	private static final int questId = 22504;

	public _22504TooLittleButterOverTooMuchBread() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801314).addOnQuestStart(questId);
		qe.registerQuestNpc(801314).addOnTalkEvent(questId);
		qe.registerQuestNpc(800528).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801314) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 4);
					}
					case QUEST_ACCEPT_1: {
						QuestService.startQuest(env);
						return sendQuestDialog(env, 1352);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 801314) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						qs.setQuestVar(1);
						return defaultCloseDialog(env, 1, 1, true, false);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800528) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
