package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog, Majka
 */
public class _2001ThinkingAhead extends AbstractQuestHandler {

	private int[] mobs = { 210369, 210368 };

	public _2001ThinkingAhead() {
		super(2001);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(203518).addOnTalkEvent(questId);
		qe.registerQuestNpc(700093).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203518) { // Boromer
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						} else if (var == 1) {
							return sendQuestDialog(env, 1352);
						} else if (var == 2) {
							return sendQuestDialog(env, 1694);
						}
						break;
					case SELECT1_1:
						playQuestMovie(env, 51);
						return sendQuestDialog(env, 1012);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
					case SETPRO3:
						return defaultCloseDialog(env, 2, 3); // 3
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 2, false, 1694, 1693);
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
				}
			} else if (targetId == 700093) {
				return true; // just give quest drop on use
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203518) { // Boromer
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2034);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		if (var >= 3 && var < 8) {
			return defaultOnKillEvent(env, mobs, 3, 8); // 3 - 8
		} else if (var == 8) {
			return defaultOnKillEvent(env, mobs, 8, true); // reward
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 2100);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 2100);
	}
}
