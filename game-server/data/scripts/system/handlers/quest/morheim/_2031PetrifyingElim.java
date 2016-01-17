package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
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
 * @author Mcrizza
 * @Modified Majka
 */
public class _2031PetrifyingElim extends QuestHandler {

	private final static int questId = 2031;

	public _2031PetrifyingElim() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182204001, questId);
		qe.registerQuestNpc(204304).addOnTalkEvent(questId); // Vili
		qe.registerQuestNpc(730038).addOnTalkEvent(questId); // Nabaru
		qe.registerOnLevelUp(questId);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (id != 182204001 || qs.getStatus() == QuestStatus.COMPLETE)
			return HandlerResult.UNKNOWN;

		if (!player.isInsideItemUseZone(ZoneName.get("DF2_ITEMUSEAREA_Q2031_220020000")))
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				playQuestMovie(env, 72);
				removeQuestItem(env, 182204001, 1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
		}, 3000);

		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2300, true); // Sets as zone mission to avoid it appears on new player list.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null)
			return false;

		if (targetId == 204304) // Vili
		{
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (dialog == DialogAction.SETPRO1) {
					qs.setQuestVar(1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return sendQuestStartDialog(env);
			}
		}
		else if (targetId == 730038) // Nabaru
		{
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (dialog == DialogAction.SELECT_ACTION_1353)
					playQuestMovie(env, 71);
				else if (dialog == DialogAction.SETPRO2) {
					qs.setQuestVar(2);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}

			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (dialog== DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					if (player.getInventory().getItemCountByItemId(182204001) > 0) {
						qs.setQuestVar(3);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					} else
						return sendQuestDialog(env, 10001);
				}
				else
					return sendQuestStartDialog(env);
			}

			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 3) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (dialog == DialogAction.SETPRO4) {
					qs.setQuestVar(4);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
			else if (qs.getStatus() == QuestStatus.REWARD)
				return sendQuestEndDialog(env);
		}

		return false;
	}
}
