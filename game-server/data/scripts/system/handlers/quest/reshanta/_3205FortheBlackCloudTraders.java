package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pad
 */
public class _3205FortheBlackCloudTraders extends AbstractQuestHandler {


	private static final int[] npcIds = { 279010, 203735, 798321 };
	private static final int mobId = 219024;

	public _3205FortheBlackCloudTraders() {
		super(3205);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcIds[0]).addOnQuestStart(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(mobId).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == npcIds[0]) { // Jielinlinerk
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcIds[0]) { // Jielinlinerk
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO2:
						return defaultCloseDialog(env, 15, 16);
				}
			} else if (targetId == npcIds[1]) { // Himusus
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1693);
					case SET_SUCCEED:
						return defaultCloseDialog(env, 16, 16, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[2]) { // Koruchinerk
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, mobId, 0, 15); // 1 - 3
	}

}
