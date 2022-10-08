package quest.brusthonin;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Nephis
 */
public class _4077PorgusRoundup extends AbstractQuestHandler {

	public _4077PorgusRoundup() {
		super(4077);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205158).addOnQuestStart(questId); // Holekk
		qe.registerQuestNpc(205158).addOnTalkEvent(questId);
		qe.registerQuestNpc(214732).addOnAttackEvent(questId);
	}

	@Override
	public boolean onAttackEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId != 214732)
			return false;

		final Npc npc = (Npc) env.getVisibleObject();
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
			if (PositionUtil.getDistance(1356, 1901, 46, npc.getX(), npc.getY(), npc.getZ()) > 10) {
				return false;
			}
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			updateQuestStatus(env);
			npc.getController().deleteAndScheduleRespawn();
			return true;
		} else if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {

			if (PositionUtil.getDistance(1356, 1901, 46, npc.getX(), npc.getY(), npc.getZ()) > 10) {
				return false;
			}
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			npc.getController().deleteAndScheduleRespawn();
			return true;
		}

		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205158) { // Holekk
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205158)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
