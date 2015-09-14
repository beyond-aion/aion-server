package quest.sanctum;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Talk with Erdos (203740). Bring the Blue Aether Powder (186000088) and Kinah (50000) to Flora (798384).
 * 
 * @author undertrey
 * @modified vlog
 */
public class _3962GrowthFlorasSecondCharm extends QuestHandler {

	private final static int questId = 3962;

	public _3962GrowthFlorasSecondCharm() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798384).addOnQuestStart(questId);
		qe.registerQuestNpc(798384).addOnTalkEvent(questId);
		qe.registerQuestNpc(203740).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798384) { // Flora
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203740: { // Erdos
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1352);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1, 182206109, 1, 0, 0); // 1
					}
				}
				case 798384: // Flora
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 1) {
								removeQuestItem(env, 182206109, 1);
								return sendQuestDialog(env, 2375);
							}
						case CHECK_USER_HAS_QUEST_ITEM:
							long itemAmount = player.getInventory().getItemCountByItemId(186000088);
							if (var == 1 && player.getInventory().tryDecreaseKinah(50000) && itemAmount >= 1) {
								removeQuestItem(env, 186000088, 1);
								changeQuestStep(env, 1, 1, true); // reward
								return sendQuestDialog(env, 5);
							} else
								return sendQuestDialog(env, 2716);
						case FINISH_DIALOG:
							return defaultCloseDialog(env, 1, 1);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798384) { // Flora
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
