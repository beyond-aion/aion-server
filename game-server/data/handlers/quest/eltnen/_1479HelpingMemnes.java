package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pad
 */
public class _1479HelpingMemnes extends AbstractQuestHandler {

	private static final int[] npcIds = { 730020, 203912, 203898 }; // Demro, Memnes, Coeus

	public _1479HelpingMemnes() {
		super(1479);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcIds[0]).addOnQuestStart(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == npcIds[0]) { // Demro
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == npcIds[1]) { // Memnes
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						} else if (var == 2) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 2, 2, true);
						return sendQuestDialog(env, 5);
				}
			} else if (targetId == npcIds[2]) { // Coeus
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1693);
						}
						return false;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2); // 2
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[1]) { // Memnes
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

}
