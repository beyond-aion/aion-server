package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Balthazar
 */
public class _1604ToCatchASpy extends AbstractQuestHandler {

	public _1604ToCatchASpy() {
		super(1604);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204576).addOnQuestStart(questId);
		qe.registerQuestNpc(204576).addOnTalkEvent(questId);
		qe.registerQuestNpc(212615).addOnAttackEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204576) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204576) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean onAttackEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 0) {
			return false;
		}

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		if (targetId != 212615) {
			return false;
		}

		if (PositionUtil.getDistance(env.getVisibleObject(), 717.78f, 623.50f, 130) < 30) {
			((Npc) env.getVisibleObject()).getController().die(player);
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);

		}
		return false;
	}
}
