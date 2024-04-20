package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Artur, Ritsu, Majka
 */
public class _14054KrallIngToKralltumagna extends AbstractQuestHandler {

	private final static int[] indratu_runaway = { 235483, 235484, 235485, 235486, 235487, 235488, 235489, 235490, 235491, 235492, 235493, 235494,
		235495, 235496, 235497, 235498, 235499, 235500, 235501, 235502 };
	private final static int baranath = 702040;
	private final static int vitusa = 233861;

	public _14054KrallIngToKralltumagna() {
		super(14054);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204602, 800413, 802050 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npc_ids)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		for (int mob_id : indratu_runaway)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
		qe.registerQuestNpc(baranath).addOnKillEvent(questId);
		qe.registerQuestNpc(vitusa).addOnKillEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14050, 14051, 14052, 14053);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14050, 14051, 14052, 14053);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204602: // Atalante
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							break;
						case SETPRO1:
							return defaultCloseDialog(env, var, var + 1); // 1
					}
					break;
				case 800413: // Javlantia
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							break;
						case SETPRO2:
							return defaultCloseDialog(env, var, var + 1); // 2
					}
					break;

				case 802050: // Bartyn
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 2034);
							if (var == 4)
								return sendQuestDialog(env, 2716);
							break;
						case SETPRO4:
							return defaultCloseDialog(env, 2, 3); // 3
						case SETPRO6:
							qs.setRewardGroup(0); // both available groups are the same
							return defaultCloseDialog(env, 4, 5, true, false); // 3
					}
					break;
			}
		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800413) { // Javlantia
				if (dialogActionId == USE_OBJECT)
					return sendQuestDialog(env, 3057);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 2) {
				return defaultOnKillEvent(env, indratu_runaway, 0, 6, 1) || defaultOnKillEvent(env, baranath, 0, 3, 2);
			}
			if (qs.getQuestVarById(0) == 3) {
				return defaultOnKillEvent(env, vitusa, 3, 4); // 4
			}
		}
		return false;
	}
}
