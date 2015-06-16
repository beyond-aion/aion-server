package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _24052AFrozenCity extends QuestHandler {

	private final static int questId = 24052;
	private final static int[] npc_ids = { 204753, 790016, 730036, 279000 };

	public _24052AFrozenCity() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182215378, questId);
		qe.registerQuestItem(182215379, questId);
		qe.registerQuestItem(182215380, questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24050, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204753) {
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					int[] questItems = { 182215378, 182215379, 182215380 };
					return sendQuestEndDialog(env, questItems);
				}
			}
		}
		else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204753) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
				case SELECT_ACTION_1012:
					playQuestMovie(env, 242);
					break;
				case SELECT_ACTION_1097:
					if (var == 0 && player.getInventory().getItemCountByItemId(182215378) != 1) {
						if (giveQuestItem(env, 182215378, 1))
							return sendQuestDialog(env, 1097);
					}
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
		if (!player.isInsideZone(ZoneName.get("DF3_ITEMUSEAREA_Q2056")))
			return HandlerResult.FAILED;

		if (id != 182215378 && qs.getQuestVarById(0) == 1 || id != 182215379 && qs.getQuestVarById(0) == 2
			|| id != 182215380 && qs.getQuestVarById(0) == 3)
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 2000, 0,
			0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0,
					1, 0), true);
				if (qs.getQuestVarById(0) == 1) {
					playQuestMovie(env, 243);
					removeQuestItem(env, id, 1);
					changeQuestStep(env, 1, 2, false); // 2
					giveQuestItem(env, 182215379, 1);
				}
				else if (qs.getQuestVarById(0) == 2) {
					playQuestMovie(env, 244);
					removeQuestItem(env, id, 1);
					changeQuestStep(env, 2, 3, false); // 3
					giveQuestItem(env, 182215380, 1);
				}
				else if (qs.getQuestVarById(0) == 3 && qs.getStatus() != QuestStatus.COMPLETE
					&& qs.getStatus() != QuestStatus.NONE) {
					removeQuestItem(env, id, 1);
					playQuestMovie(env, 245);
					changeQuestStep(env, 3, 3, true); // reward
				}
			}
		}, 2000);
		return HandlerResult.SUCCESS;
	}
}