package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rhys2002
 */
public class _1114TheNymphsGown extends QuestHandler {

	private final static int questId = 1114;
	private final static int[] npc_ids = { 203075, 203058, 700008 };

	public _1114TheNymphsGown() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182200214, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 0) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				if (env.getDialogId() == DialogAction.QUEST_ACCEPT_1.id()) {
					QuestService.startQuest(env);
					if (!giveQuestItem(env, 182200226, 1))
						;
					removeQuestItem(env, 182200214, 1); // Namus's Diary with double-click to start the
																							// quest
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
				}
				else
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203075 && var == 4) // Namus
			{
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 6);
				else
					return sendQuestEndDialog(env);
			}
			else if (targetId == 203058 && var == 3) // Asteros
				return sendQuestEndDialog(env);
		}
		else if (qs.getStatus() != QuestStatus.START)
			return false;

		if (targetId == 203075) // Namus
		{
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 3)
						return sendQuestDialog(env, 2375);

				case SELECT_QUEST_REWARD:
					if (var == 2) {
						qs.setQuestVarById(0, var + 2);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182200217, 1);
						return sendQuestDialog(env, 6);
					}
					if (var == 3) {
						qs.setQuestVarById(0, var + 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182200217, 1);
						return sendQuestDialog(env, 6);
					}
				case SETPRO1:
					if (var == 0) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						removeQuestItem(env, 182200226, 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case SETPRO2:
					if (var == 2) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if (targetId == 700008) { // Seirenia's clothes
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 1) {
						for (VisibleObject obj : player.getKnownList().getKnownObjects().values()) {
							if (!(obj instanceof Npc))
								continue;
							if (((Npc) obj).getNpcId() != 203175) // Seirenia
								continue;
							((Npc) obj).getAggroList().addDamage(player, 50);
						}
						// Nymph's Dress
						if (!giveQuestItem(env, 182200217, 1))
							; // wtf ?
						qs.setQuestVarById(0, 2);
						updateQuestStatus(env);
					}
					return true;
			}
		}
		if (targetId == 203058) // Asteros
		{
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2034);
				case SETPRO3:
					if (var == 3) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182200217, 1);
						return sendQuestDialog(env, 5);
					}
				case SETPRO2:
					if (var == 3) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (id != 182200214)
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			QuestService.startQuest(env);
		}
		return HandlerResult.SUCCESS;
	}
}
