package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */
public class _3326TheShugoMenace extends AbstractQuestHandler {

	public _3326TheShugoMenace() {
		super(3326);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798053).addOnQuestStart(questId);
		qe.registerQuestNpc(798053).addOnTalkEvent(questId);
		qe.registerQuestNpc(210897).addOnKillEvent(questId);
		qe.registerQuestNpc(210939).addOnKillEvent(questId);
		qe.registerQuestNpc(210873).addOnKillEvent(questId);
		qe.registerQuestNpc(210919).addOnKillEvent(questId);
		qe.registerQuestNpc(211754).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798053) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4);
				} else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798053) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						if (qs.getQuestVarById(0) != 20) {
							return false;
						}
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798053) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 210897 || targetId == 210939 || targetId == 210873 || targetId == 210919 || targetId == 211754) {
			if (var >= 0 && var < 20) {
				qs.setQuestVarById(0, var + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
