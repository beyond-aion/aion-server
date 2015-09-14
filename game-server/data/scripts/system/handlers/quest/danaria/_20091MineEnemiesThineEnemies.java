package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _20091MineEnemiesThineEnemies extends QuestHandler {

	private final static int questId = 20091;

	public _20091MineEnemiesThineEnemies() {
		super(questId);
	}

	@Override
	public void register() {
		int[] mobs = { 231414, 231415 };
		qe.registerOnLevelUp(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestNpc(801155).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20090);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 801155) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						} else if (qs.getQuestVarById(0) == 1) {
							return sendQuestDialog(env, 1352);
						} else if (qs.getQuestVarById(0) == 2) {
							return sendQuestDialog(env, 10000);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 1, 2, false, 10000, 10001);
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 2, 3);
					}
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801155) {
				if (dialog == DialogAction.USE_OBJECT) {
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
		return defaultOnKillEvent(env, 231415, 3, 6) || defaultOnKillEvent(env, 231414, 6, true);
	}
}
