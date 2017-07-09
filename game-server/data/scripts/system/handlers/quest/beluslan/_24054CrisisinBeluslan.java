package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _24054CrisisinBeluslan extends AbstractQuestHandler {

	public _24054CrisisinBeluslan() {
		super(24054);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204701, 204702, 802053 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(702041).addOnKillEvent(questId);
		qe.registerQuestNpc(233865).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 24053, 24052, 24051, 24050 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 24053, 24052, 24051, 24050 };
		defaultOnLevelChangedEvent(player, quests);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		switch (env.getTargetId()) {
			case 702041:
				if (qs.getQuestVarById(0) >= 2 && qs.getQuestVarById(0) < 5) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
				}
				break;
			case 233865:
				if (qs.getQuestVarById(0) == 5) {
					changeQuestStep(env, 5, 6); // 6
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204702) { // Nerita
				if (dialogActionId == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204702) { // Nerita
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					break;
				case SELECT1_2:
					playQuestMovie(env, 255);
					break;
				case SETPRO1:
					if (var == 0)
						return defaultCloseDialog(env, 0, 1); // 1
			}
		} else if (targetId == 802053) { // Fafner
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					break;
				case SETPRO2:
					if (var == 1)
						return defaultCloseDialog(env, 1, 2); // 2
			}
		} else if (targetId == 204701) { // Hod
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 6)
						return sendQuestDialog(env, 2375);
					break;
				case SET_SUCCEED:
					if (var == 6)
						return defaultCloseDialog(env, 6, 6, true, false); // reward
			}
		}
		return false;
	}
}
