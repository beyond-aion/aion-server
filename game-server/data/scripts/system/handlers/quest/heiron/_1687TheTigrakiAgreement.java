package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Go to Draupnir Cave in Asmodae and get Blue Balaur Blood (186000035) (2) and Balaur Rainbow Scales (186000036) (5) for Brosia (204601). Go to
 * Brosia to choose your reward.
 * 
 * @author Balthazar
 * @reworked vlog
 */

public class _1687TheTigrakiAgreement extends QuestHandler {

	private final static int questId = 1687;
	private int rewardGroup;

	public _1687TheTigrakiAgreement() {
		super(questId);
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

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 204601) { // Brosia
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204601) { // Brosia
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case CHECK_USER_HAS_QUEST_ITEM: {
						long collect1 = player.getInventory().getItemCountByItemId(186000035);
						long collect2 = player.getInventory().getItemCountByItemId(186000036);
						if (collect1 >= 2 && collect2 >= 5) {
							removeQuestItem(env, 186000035, 2);
							removeQuestItem(env, 186000036, 5);
							return sendQuestDialog(env, 1352); // choose your reward
						} else
							return sendQuestDialog(env, 1097);
					}
					case FINISH_DIALOG:
						return defaultCloseDialog(env, var, var);
					case SETPRO10: {
						rewardGroup = 0;
						return defaultCloseDialog(env, var, var, true, true, 0); // reward 1
					}
					case SETPRO20: {
						rewardGroup = 1;
						return defaultCloseDialog(env, var, var, true, true, 1); // reward 2
					}
					case SETPRO30: {
						rewardGroup = 2;
						return defaultCloseDialog(env, var, var, true, true, 2); // reward 3
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204601) { // Brosia
				return sendQuestEndDialog(env, rewardGroup);
			}
		}
		return false;
	}
}
