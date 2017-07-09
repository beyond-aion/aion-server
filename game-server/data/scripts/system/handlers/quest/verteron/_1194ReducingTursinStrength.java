package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Balthazar
 */
public class _1194ReducingTursinStrength extends AbstractQuestHandler {

	public _1194ReducingTursinStrength() {
		super(1194);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203098).addOnQuestStart(questId);
		qe.registerQuestNpc(203098).addOnTalkEvent(questId);
		qe.registerQuestNpc(210185).addOnKillEvent(questId);
		qe.registerQuestNpc(210186).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("TURSIN_GARRISON_210030000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203098) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203098) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1352);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("TURSIN_GARRISON_210030000"))
			return false;

		final Player player = env.getPlayer();
		if (player == null)
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null)
			return false;

		if (qs.getQuestVarById(0) == 0) {
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}

		int targetId = 0;
		int questVar = qs.getQuestVarById(0);

		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 210185 || targetId == 210186) {
			if (questVar >= 1 && questVar < 10) {
				if (questVar == 9)
					qs.setStatus(QuestStatus.REWARD);
				else
					qs.setQuestVarById(0, questVar + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
