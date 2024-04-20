package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Artur, Majka
 */
public class _14011FragmentsInTheSky extends AbstractQuestHandler {

	public _14011FragmentsInTheSky() {
		super(14011);
	}

	@Override
	public void register() {
		int[] talkNpcs = { 203109, 203122 };
		qe.registerQuestNpc(700091).addOnKillEvent(questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int id : talkNpcs)
			qe.registerQuestNpc(id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 2 && var < 4) {
				return defaultOnKillEvent(env, 700091, 2, 4); // 3, 4
			} else if (var == 4) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203109) { // Kairon
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						} else if (var == 5) {
							return sendQuestDialog(env, 1693);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
				}
			} else if (targetId == 203122) { // Hynops
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SETPRO2:
						playQuestMovie(env, 24);
						return defaultCloseDialog(env, 1, 2); // 2
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203109) { // Kairon
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 1693);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14010);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14010);
	}
}
