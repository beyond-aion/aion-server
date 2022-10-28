package quest.the_eternal_bastion;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;
import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _18036BastionsAreEternal extends AbstractQuestHandler {

	private static final int START_NPC_ID = 801281; // Demades
	private static final int TALK_NPC_ID = 802008; // Kvash

	public _18036BastionsAreEternal() {
		super(18036);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(START_NPC_ID).addOnQuestStart(questId);
		qe.registerQuestNpc(START_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(TALK_NPC_ID).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		int targetId = env.getTargetId();
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == TALK_NPC_ID) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);

				if (dialogActionId == SETPRO1) {
					changeQuestStep(env, 0, 0, true);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == START_NPC_ID)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
