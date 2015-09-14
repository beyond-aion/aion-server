package quest.argent_manor;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _30452UndoingSpellweaver extends QuestHandler {

	private static final int questId = 30452;

	public _30452UndoingSpellweaver() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799535).addOnQuestStart(questId);
		qe.registerQuestNpc(799535).addOnTalkEvent(questId);
		qe.registerQuestNpc(799537).addOnTalkEvent(questId);
		qe.registerQuestNpc(217242).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 799535) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 799537: {
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1)
								return sendQuestDialog(env, 1352);
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 1, 1, true, false);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799535)
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 217242, 0, 1);
	}
}
