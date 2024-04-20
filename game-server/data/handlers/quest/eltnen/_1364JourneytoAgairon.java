package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Starts Ernia (203944). Take Teos (203945) to Dellome (790007) in Agairon Village. Talk to Dellome.
 * 
 * @author Rhys2002, vlog
 */
public class _1364JourneytoAgairon extends AbstractQuestHandler {

	public _1364JourneytoAgairon() {
		super(1364);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203944).addOnQuestStart(questId);
		qe.registerOnLogOut(questId);
		qe.registerQuestNpc(203945).addOnTalkEvent(questId);
		qe.registerQuestNpc(790007).addOnTalkEvent(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203944) { // Ernia
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203945: // Teos
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (qs.getQuestVarById(0) == 0)
								return sendQuestDialog(env, 1693);
							return false;
						case SETPRO1:
							return defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 790007, 0, 1); // 1
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790007) { // Dellome
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 1) {
				changeQuestStep(env, 1, 0);
			}
		}
		return false;
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		changeQuestStep(env, 1, 3);
		return defaultFollowEndEvent(env, 3, 3, true, 47); // reward
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 1, 0, false); // 0
	}
}
