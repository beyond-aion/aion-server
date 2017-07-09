package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Atomics
 */
public class _1367MabangtahsFeast extends AbstractQuestHandler {

	private static final int ITEM_ID_1 = 182201331;
	private static final int ITEM_ID_2 = 182201332;
	private static final int ITEM_ID_3 = 182201333;

	private long itemCount1, itemCount2, itemCount3;

	public _1367MabangtahsFeast() {
		super(1367);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204023).addOnQuestStart(questId);
		qe.registerQuestNpc(204023).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 204023) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.START) {
				itemCount1 = player.getInventory().getItemCountByItemId(ITEM_ID_1);
				itemCount2 = player.getInventory().getItemCountByItemId(ITEM_ID_2);
				itemCount3 = player.getInventory().getItemCountByItemId(ITEM_ID_3);
				if (env.getDialogActionId() == QUEST_SELECT && qs.getQuestVarById(0) == 0) {
					if (itemCount1 >= 1 || itemCount2 >= 5 || itemCount3 >= 2) {
						return sendQuestDialog(env, 1352);
					} else {
						return sendQuestDialog(env, 1693);
					}
				} else if (env.getDialogActionId() == SETPRO1) {
					if (itemCount1 >= 1) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItems(env);
						qs.setRewardGroup(0);
						return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
					} else
						return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SETPRO2) {
					if (itemCount2 >= 5) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItems(env);
						qs.setRewardGroup(1);
						return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
					} else
						return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SETPRO3) {
					if (itemCount3 >= 2) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItems(env);
						qs.setRewardGroup(2);
						return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
					} else
						return sendQuestDialog(env, 1352);
				} else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	private void removeQuestItems(QuestEnv env) {
		removeQuestItem(env, ITEM_ID_1, itemCount1);
		removeQuestItem(env, ITEM_ID_2, itemCount2);
		removeQuestItem(env, ITEM_ID_3, itemCount3);
	}

}
