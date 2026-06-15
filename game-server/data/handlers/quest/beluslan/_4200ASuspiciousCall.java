package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author kecimis
 */
public class _4200ASuspiciousCall extends AbstractQuestHandler {

	private final static int[] npc_ids = { 204839, 798332, 700522, 279006, 204286 };

	/*
	 * 204839 - Uikinerk 798332 - Haorunerk 700522 - Haorunerks Bag 279006 - Garkbinerk 204286 - Payrinrinerk
	 */

	public _4200ASuspiciousCall() {
		super(4200);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204839).addOnQuestStart(questId); // Uikinerk
		qe.registerQuestItem(182209097, questId); // Teleport Scroll
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204839) { // Uikinerk
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204286)// Payrinrinerk
			{
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204839 && var == 0) { // Uikinerk
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1003);
					case SETPRO1:
						WorldMapInstance steelRake = InstanceService.getNextAvailableInstance(WorldMapType.STEEL_RAKE.getId(), player);
						Npc haorunerksCorpse = steelRake.getNpc(798333);
						spawn(798332, steelRake, haorunerksCorpse.getX(), haorunerksCorpse.getY(), haorunerksCorpse.getZ(), (byte) 30);
						haorunerksCorpse.getController().delete();
						TeleportService.teleportTo(player, steelRake, 403.55f, 508.11f, 885.77f);
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 798332 && var == 1) { // Haorunerk
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SELECT2_1:
						playQuestMovie(env, 431);
						break;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 700522 && var == 2) { // Haorunerks Bag, loc: 401.24 503.19 885.76 119
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						updateQuestStatus(env);
					}
				}, 3000);
				return true;
			} else if (targetId == 279006 && var == 3) { // Garkbinerk
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2034);
					case SET_SUCCEED:
						return defaultCloseDialog(env, 3, 3, true, false);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (id != 182209097 || qs == null)
			return HandlerResult.UNKNOWN;

		if (qs.getQuestVarById(0) != 2)
			return HandlerResult.FAILED;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				removeQuestItem(env, 182209097, 1);
				// teleport location(BlackCloudIsland): 400010000 3419.16 2445.43 2766.54 57
				TeleportService.teleportTo(player, 400010000, 3419.16f, 2445.43f, 2766.54f, (byte) 57);
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(env);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}
}
