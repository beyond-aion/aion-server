package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Go to Draupnir Cave in Asmodae and get Blue Balaur Blood (186000035) (2) and Balaur Rainbow Scales (186000036) (5) for Brosia (204601). Go to
 * Brosia to choose your reward.
 * 
 * @author Balthazar, vlog
 */
public class _1687TheTigrakiAgreement extends AbstractQuestHandler {

	public _1687TheTigrakiAgreement() {
		super(1687);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204601).addOnQuestStart(questId);
		qe.registerQuestNpc(204601).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204601) { // Brosia
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204601) { // Brosia
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case CHECK_USER_HAS_QUEST_ITEM:
						long collect1 = player.getInventory().getItemCountByItemId(186000035);
						long collect2 = player.getInventory().getItemCountByItemId(186000036);
						if (collect1 >= 2 && collect2 >= 5) {
							removeQuestItem(env, 186000035, 2);
							removeQuestItem(env, 186000036, 5);
							return sendQuestDialog(env, 1352); // choose your reward
						} else
							return sendQuestDialog(env, 1097);
					case FINISH_DIALOG:
						return defaultCloseDialog(env, var, var);
					case SETPRO10:
						qs.setRewardGroup(0);
						return defaultCloseDialog(env, var, var, true, true); // reward 1
					case SETPRO20:
						qs.setRewardGroup(1);
						return defaultCloseDialog(env, var, var, true, true); // reward 2
					case SETPRO30:
						qs.setRewardGroup(2);
						return defaultCloseDialog(env, var, var, true, true); // reward 3
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204601) { // Brosia
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
