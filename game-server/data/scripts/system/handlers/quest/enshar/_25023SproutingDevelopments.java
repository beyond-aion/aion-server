package quest.enshar;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @Author Majka
 */
public class _25023SproutingDevelopments extends QuestHandler {

	private final static int questId = 25023;

	public _25023SproutingDevelopments() {
		super(questId);
	}

	@Override
	public void register() {
		// Malthorn 804909
		// Varla 804910
		qe.registerQuestNpc(804909).addOnQuestStart(questId);
		int[] npcs = { 804909, 804910 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182215711, questId); // Enriched Seed
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 804909) { // Malthorn
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 804909: // Malthorn
					if (var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
							return checkQuestItems(env, var, var + 1, false, 10000, 10001, 182215709, 1);
					}
					if (var == 1) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialog == DialogAction.SETPRO2)
							return defaultCloseDialog(env, var, var + 1, 182215711, 1);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804910: // Varla
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return HandlerResult.UNKNOWN;

		final int id = item.getItemTemplate().getTemplateId();
		if (id != 182215711)
			return HandlerResult.UNKNOWN;

		final int itemObjId = item.getObjectId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 1000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				int var = qs.getQuestVarById(0);
				if (var == 2) {
					float xPlayer = player.getPosition().getX();
					float yPlayer = player.getPosition().getY();
					float zPlayer = player.getPosition().getZ();
					QuestService.addNewSpawn(220080000, player.getInstanceId(), 805161, xPlayer + 2, yPlayer + 2, zPlayer, (byte) 60, 2);
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
				}
			}
		}, 1000);
		return HandlerResult.SUCCESS;
	}
}
