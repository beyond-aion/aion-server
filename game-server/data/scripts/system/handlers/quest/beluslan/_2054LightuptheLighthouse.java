package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
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
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002
 * @Modified Majka
 */
public class _2054LightuptheLighthouse extends QuestHandler {

	private final static int questId = 2054;
	private final static int[] npc_ids = { 204768, 204739, 730109, 730140, 700287 };

	public _2054LightuptheLighthouse() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182204308, questId);
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2500, true); // Sets as zone mission to avoid it appears on new player list.
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final Npc npc = (Npc) env.getVisibleObject();

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204768) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (dialog == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204768) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
				case SETPRO1:
					if (var == 0) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if (targetId == 204739) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
				case SELECT_ACTION_1353:
					playQuestMovie(env, 237);
					break;
				case SETPRO2:
					if (var == 1) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if (targetId == 730109) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2034);
				case SETPRO4:
					if (var == 3) {
						QuestService.addNewSpawn(220040000, player.getInstanceId(), 213912, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
						npc.getController().scheduleRespawn();
						npc.getController().onDelete();
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
						return true;
					}
			}
		}
		else if (targetId == 730140) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2120);
				case SETPRO5:
					if (var == 3) {
						if (!giveQuestItem(env, 182204309, 1))
							return true;
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						npc.getController().scheduleRespawn();
						npc.getController().onDelete();
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if (targetId == 700287 && qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.USE_OBJECT) {
				return useQuestObject(env, 4, 4, true, 0, 0, 0, 182204309, 1, 238, false); // movie + reward
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if (id != 182204308)
			return HandlerResult.UNKNOWN;

		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getQuestVarById(0) != 2)
			return HandlerResult.FAILED;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 1000, 0,
			0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0,
					1, 0), true);
				removeQuestItem(env, 182204308, 1);
				qs.setQuestVarById(0, 3);
				updateQuestStatus(env);
			}
		}, 1000);
		return HandlerResult.SUCCESS;
	}
}
