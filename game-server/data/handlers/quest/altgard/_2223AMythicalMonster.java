package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke, Gigi, vlog
 */
public class _2223AMythicalMonster extends AbstractQuestHandler {

	public _2223AMythicalMonster() {
		super(2223);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203616).addOnQuestStart(questId);
		qe.registerQuestNpc(203616).addOnTalkEvent(questId);
		qe.registerQuestNpc(700134).addOnTalkEvent(questId);
		qe.registerQuestNpc(211621).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203616) { // Gefion
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203620: // Lamir
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							} else if (var == 1) {
								if (player.getInventory().getItemCountByItemId(182203217) == 1) {
									return sendQuestDialog(env, 1694);
								} else {
									giveQuestItem(env, 182203217, 1);
									return sendQuestDialog(env, 1779);
								}
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1, 182203217, 1, 0, 0); // 1
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 700134: // Old Incense Burner
					if (env.getDialogActionId() == USE_OBJECT) {
						if (player.getInventory().getItemCountByItemId(182203217) == 1) {
							return useQuestObject(env, 1, 1, false, 0, 0, 0, 182203217, 1, 67, true); // movie + die
						}
					}
					break;
				case 203616: // Gefion
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 2716);
							}
							return false;
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203616) { // Gefion
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
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
		return defaultOnKillEvent(env, 211621, 1, true); // reward
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 67)
			spawnForFiveMinutes(211621, env.getPlayer().getWorldMapInstance(), (float) 1547.1047, (float) 894.2969, (float) 248.019, (byte) 85);
	}
}
