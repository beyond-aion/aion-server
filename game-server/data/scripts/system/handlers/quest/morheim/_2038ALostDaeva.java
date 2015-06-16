package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Hellboy aion4Free
 * @reworked vlog
 */
public class _2038ALostDaeva extends QuestHandler {

	private final static int questId = 2038;

	public _2038ALostDaeva() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204342, 204053, 700233 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(212879).addOnKillEvent(questId);
		for (int npc_id : npcs)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2300, true);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204342: { // Mirka
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							if (var == 4)
								return sendQuestDialog(env, 2375);
						}
						case SELECT_ACTION_1012: {
							if (var == 0) {
								playQuestMovie(env, 82);
								return sendQuestDialog(env, 1012);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 4, 4, true, false, 0, 0, 182204016, 1); // reward
						}
					}
					break;
				}
				case 700233: { // Pagimkin's Corpse
					if (dialog == DialogAction.USE_OBJECT && var == 1) {
						return useQuestObject(env, 1, 2, false, 0); // 2
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204053) { // Kvasir
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("WONSHIKUTZS_LABORATORY_220020000"))) {
				int var = qs.getQuestVarById(0);
				if (var == 1 || var == 2) {
					return playQuestMovie(env, 83);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2 && env.getTargetId() == 212879) {
				changeQuestStep(env, 2, 4, false); // 4
				return true;
			}
		}
		return false;
	}
}
