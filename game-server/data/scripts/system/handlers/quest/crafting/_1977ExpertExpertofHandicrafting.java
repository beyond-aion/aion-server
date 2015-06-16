package quest.crafting;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _1977ExpertExpertofHandicrafting extends QuestHandler {

	private final static int questId = 1977;

	public _1977ExpertExpertofHandicrafting() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203792).addOnQuestStart(questId);
		qe.registerQuestNpc(203792).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203792) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203792: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182206896);
							if (itemCount1 > 0) {
								removeQuestItem(env, 182206896, 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 2375);
							}
							else
								return sendQuestDialog(env, 2716);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203792) {
				if (env.getDialogId() == DialogAction.CHECK_USER_HAS_QUEST_ITEM.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
