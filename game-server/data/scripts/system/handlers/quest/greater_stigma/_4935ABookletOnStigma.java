package quest.greater_stigma;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author kecimis
 */
public class _4935ABookletOnStigma extends QuestHandler {

	private final static int questId = 4935;
	private final static int[] npc_ids = { 204051, 204285, 279005 };

	/*
	 * 204051 - Vergelmir 204285 - Teirunerk 279005 - Kohrunerk 182207104 - Pirates Research Log
	 */

	public _4935ABookletOnStigma() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204051).addOnQuestStart(questId); // Vergelmir
		qe.registerQuestItem(182207107, questId); // Teirunerks Letter
		qe.registerQuestItem(182207108, questId); // Tattered Booklet
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204051)// Vergelmir
			{
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);

			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.REWARD) {

			if (targetId == 204051 && player.getInventory().getItemCountByItemId(182207108) == 1)// Vergelmir
			{
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204285)// Teirunerk
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						if (var == 1)
							return sendQuestDialog(env, 1352);
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 1) {
							if (QuestService.collectItemCheck(env, true)) {
								if (!giveQuestItem(env, 182207107, 1))
									return true;
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							}
							else
								return sendQuestDialog(env, 10001);
						}
					case SETPRO1:
						if (var == 0)
							qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
				return false;
			}
			else if (targetId == 279005 && player.getInventory().getItemCountByItemId(182207107) == 1)// Kohrunerk
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 2)
							return sendQuestDialog(env, 1693);
					case SET_SUCCEED:
						if (var == 2)
							removeQuestItem(env, 182207107, 1);
						giveQuestItem(env, 182207108, 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return true;
				}

			}
			return false;
		}
		return false;
	}

}
