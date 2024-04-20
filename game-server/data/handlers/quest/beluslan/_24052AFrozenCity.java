package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu, Majka
 */
public class _24052AFrozenCity extends AbstractQuestHandler {

	public _24052AFrozenCity() {
		super(24052);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204753, 790016, 730036, 279000 };
		qe.registerQuestItem(182215378, questId);
		qe.registerQuestItem(182215379, questId);
		qe.registerQuestItem(182215380, questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24050);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24050);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204753) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				int[] questItems = { 182215378, 182215379, 182215380 };
				return sendQuestEndDialog(env, questItems);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204753) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					return false;
				case SELECT1_1:
					playQuestMovie(env, 242);
					break;
				case SELECT1_2:
					if (var == 0 && player.getInventory().getItemCountByItemId(182215378) != 1) {
						if (giveQuestItem(env, 182215378, 1))
							return sendQuestDialog(env, 1097);
					}
					return false;
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1); // 1
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
		if (!player.isInsideItemUseZone(ZoneName.get("DF3_ITEMUSEAREA_Q2056")))
			return HandlerResult.FAILED;

		if (id != 182215378 && qs.getQuestVarById(0) == 1 || id != 182215379 && qs.getQuestVarById(0) == 2
			|| id != 182215380 && qs.getQuestVarById(0) == 3)
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 2000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				if (qs.getQuestVarById(0) == 1) {
					playQuestMovie(env, 243);
					removeQuestItem(env, id, 1);
					changeQuestStep(env, 1, 2); // 2
					giveQuestItem(env, 182215379, 1);
				} else if (qs.getQuestVarById(0) == 2) {
					playQuestMovie(env, 244);
					removeQuestItem(env, id, 1);
					changeQuestStep(env, 2, 3); // 3
					giveQuestItem(env, 182215380, 1);
				} else if (qs.getQuestVarById(0) == 3 && qs.getStatus() != QuestStatus.COMPLETE) {
					removeQuestItem(env, id, 1);
					playQuestMovie(env, 245);
					qs.setQuestVar(4);
					changeQuestStep(env, 4, 4, true); // reward
				}
			}
		}, 2000);
		return HandlerResult.SUCCESS;
	}
}
