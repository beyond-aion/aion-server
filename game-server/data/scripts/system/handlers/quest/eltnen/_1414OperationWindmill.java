package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Xitanium
 */
public class _1414OperationWindmill extends AbstractQuestHandler {

	public _1414OperationWindmill() {
		super(1414);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203989).addOnQuestStart(questId); // Tumblusen
		qe.registerQuestNpc(203989).addOnTalkEvent(questId);
		qe.registerQuestNpc(700175).addOnTalkEvent(questId); // Old Gear
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 203989) { // Tumblusen
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182201349, 1))
						return sendQuestStartDialog(env);
					else
						return true;
				} else
					return sendQuestStartDialog(env);
			}

		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		} else if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
			switch (targetId) {
				case 700175: // Old Gear
					if (qs.getQuestVarById(0) == 0 && env.getDialogActionId() == USE_OBJECT) {
						qs.setQuestVar(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182201349, 1);
						return true;
					}
			}
		}
		return false;
	}
}
