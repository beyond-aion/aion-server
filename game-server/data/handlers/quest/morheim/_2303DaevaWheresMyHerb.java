package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class _2303DaevaWheresMyHerb extends AbstractQuestHandler {

	public _2303DaevaWheresMyHerb() {
		super(2303);
	}

	@Override
	public void register() {
		int[] mobs = { 211298, 211305, 211304, 211297 };
		qe.registerQuestNpc(798082).addOnQuestStart(questId); // Bicorunerk
		qe.registerQuestNpc(798082).addOnTalkEvent(questId); // Bicorunerk
		qe.registerQuestNpc(204378).addOnTalkEvent(questId); // Favyr
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798082) { // Bicorunerk
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_1:
						QuestService.startQuest(env);
				}
				return super.onDialogEvent(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 798082) { // Bicorunerk
				if (dialogActionId == FINISH_DIALOG) {
					return sendQuestSelectionDialog(env);
				} else if (dialogActionId == USE_OBJECT) {
					if (var == 0) {
						return sendQuestDialog(env, 1003);
					} else {
						return sendQuestSelectionDialog(env);
					}
				} else if (dialogActionId == SETPRO10) {
					changeQuestStep(env, 0, 11); // 11
					qs.setRewardGroup(0);
					return sendQuestDialog(env, 1012);
				} else if (dialogActionId == SETPRO20) {
					changeQuestStep(env, 0, 21); // 21
					qs.setRewardGroup(1);
					return sendQuestDialog(env, 1097);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204378) { // Favyr
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 15) {
							return sendQuestDialog(env, 1353);
						} else if (var == 25) {
							return sendQuestDialog(env, 1438);
						}
						return false;
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
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			int[] daru = { 211298, 211305 };
			int[] ettins = { 211304, 211297 };
			if (var >= 11 && var < 15) {
				return defaultOnKillEvent(env, daru, 10, 15); // 15
			} else if (var == 15) {
				switch (targetId) {
					case 211298:
					case 211305:
						qs.setQuestVar(15);
						qs.setStatus(QuestStatus.REWARD); // reward
						updateQuestStatus(env);
						return true;
				}
			} else if (var >= 21 && var < 25) {
				return defaultOnKillEvent(env, ettins, 20, 25); // 25
			} else if (var == 25) {
				switch (targetId) {
					case 211304:
					case 211297:
						qs.setQuestVar(25);
						qs.setStatus(QuestStatus.REWARD); // reward
						updateQuestStatus(env);
						return true;
				}
			}
		}
		return false;
	}
}
