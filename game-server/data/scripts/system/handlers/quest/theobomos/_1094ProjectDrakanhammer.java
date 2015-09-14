package quest.theobomos;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class _1094ProjectDrakanhammer extends QuestHandler {

	private final static int questId = 1094;

	public _1094ProjectDrakanhammer() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203834, 798155, 700411, 730153 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 1093);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 1091, 1093 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (var) {
				case 0: {
					if (targetId == 203834) { // Nestor
						switch (dialog) {
							case QUEST_SELECT: {
								return sendQuestDialog(env, 1011);
							}
							case SETPRO1: {
								return defaultCloseDialog(env, 0, 1); // 1
							}
						}
					}
				}
				case 1: {
					if (targetId == 798155) { // Atropos
						switch (dialog) {
							case QUEST_SELECT: {
								return sendQuestDialog(env, 1352);
							}
							case SELECT_ACTION_1353: {
								playQuestMovie(env, 367);
								break;
							}
							case SETPRO2: {
								return defaultCloseDialog(env, 1, 2); // 2
							}
						}
					}
				}
				case 2: {
					if (targetId == 700411) { // Research Diary
						if (dialog == DialogAction.USE_OBJECT) {
							if (giveQuestItem(env, 182208017, 1)) {
								closeDialogWindow(env);
								changeQuestStep(env, 2, 3, false); // 3
								return true;
							}
						}
					}
				}
				case 3: {
					if (targetId == 730153) { // Assistant's Journal
						if (dialog == DialogAction.USE_OBJECT) {
							QuestService.collectItemCheck(env, true);
							removeQuestItem(env, 182208017, 1);
							qs.setQuestVar(4); // 4
							qs.setStatus(QuestStatus.REWARD); // reward
							updateQuestStatus(env);
							return true;
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203834) { // Nestor
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
