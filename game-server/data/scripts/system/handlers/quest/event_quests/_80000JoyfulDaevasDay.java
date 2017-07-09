package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rolandas
 */
public class _80000JoyfulDaevasDay extends AbstractQuestHandler {

	public _80000JoyfulDaevasDay() {
		super(80000);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798415).addOnTalkEvent(questId); // Ias
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (env.getTargetId() == 0) {
			if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				QuestService.startEventQuest(env, QuestStatus.START);
				closeDialogWindow(env);
				return true;
			}
		} else if (env.getTargetId() == 798415) { // Ias
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					qs.setQuestVar(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
