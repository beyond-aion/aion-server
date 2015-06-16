package quest.abyssal_splinter;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rikka & vlog
 */
public class _30355TheProtectorsMadness extends QuestHandler {

	private final static int questId = 30355;

	public _30355TheProtectorsMadness() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(260265).addOnQuestStart(questId);
		qe.registerQuestNpc(260265).addOnTalkEvent(questId);
		qe.registerQuestNpc(700856).addOnTalkEvent(questId);
		qe.registerQuestNpc(278001).addOnTalkEvent(questId);
		qe.registerQuestNpc(216952).addOnKillEvent(questId);
		qe.registerQuestNpc(216960).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 260265) { // Gwal
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 700856) { // Artifact of Protection
				switch (dialog) {
					case USE_OBJECT: {
						if (var == 1) {
							return useQuestObject(env, 1, 1, true, false);
						}
						break;
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 278001) { // Votan
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int[] mobs = { 216952, 216960 };
		return defaultOnKillEvent(env, mobs, 0, 1); // 1
	}
}
