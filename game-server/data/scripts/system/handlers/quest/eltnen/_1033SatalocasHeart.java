package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Xitanium
 * @reworked vlog
 */
public class _1033SatalocasHeart extends QuestHandler {

	private final static int questId = 1033;

	public _1033SatalocasHeart() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLogOut(questId);
		qe.registerQuestNpc(203900).addOnTalkEvent(questId);
		qe.registerQuestNpc(203996).addOnTalkEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203900: { // Diomedes
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 203996: { // Kimeia
					long drakeFangs = player.getInventory().getItemCountByItemId(182201019);
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1693);
							} else if (var >= 10) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SELECT_ACTION_1695: {
							playQuestMovie(env, 42);
							return sendQuestDialog(env, 1695);
						}
						case SETPRO3: {
							QuestService.questTimerStart(env, 180);
							return defaultCloseDialog(env, 1, 10);
						}
						case SELECT_ACTION_2035: {
							if (drakeFangs < 5) {
								QuestService.questTimerEnd(env);
								qs.setQuestVar(1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 2035);
							} else if (drakeFangs >= 5 && drakeFangs < 7) {
								removeQuestItem(env, 182201019, drakeFangs);
								qs.setQuestVar(12);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								QuestService.questTimerEnd(env);
								return sendQuestDialog(env, 2120);
							} else if (drakeFangs >= 7) {
								removeQuestItem(env, 182201019, drakeFangs);
								qs.setQuestVar(13);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								QuestService.questTimerEnd(env);
								return sendQuestDialog(env, 2205);
							}
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203900) { // Diomedes
				if (dialog == DialogAction.USE_OBJECT) {
					switch (var) {
						case 12: {
							return sendQuestDialog(env, 2716);
						}
						case 13: {
							return sendQuestDialog(env, 3057);
						}
					}
				} else {
					return sendQuestEndDialog(env, var - 12);
				}
			} else if (targetId == 203996) { // Kimeia
				if (dialog == DialogAction.FINISH_DIALOG) {
					return sendQuestSelectionDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		changeQuestStep(env, 10, 11, false);
		return true;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 10) {
				changeQuestStep(env, 10, 1, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1300, true);
	}
}
