package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke, Xitanium, Pad
 */
public class _1471FakeStigma extends AbstractQuestHandler {

	public _1471FakeStigma() {
		super(1471);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203991).addOnQuestStart(questId); // Dionera
		qe.registerQuestNpc(203991).addOnTalkEvent(questId); // Dionera
		qe.registerQuestNpc(203703).addOnTalkEvent(questId); // Likesan
		qe.registerQuestNpc(798321).addOnTalkEvent(questId);// Koruchinerk
		qe.registerQuestNpc(798024).addOnTalkEvent(questId); // Kierunerk
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203991) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var0 = qs.getQuestVarById(0);
			if (targetId == 203703) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var0 == 0)
							return sendQuestDialog(env, 1352);
						else if (var0 == 3)
							return sendQuestDialog(env, 2375);
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 3, 3, true, true);
				}
			} else if (targetId == 798024) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1693);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 798321) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2034);
					case SETPRO3:
						return defaultCloseDialog(env, 2, 3);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203703) {
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}
}
