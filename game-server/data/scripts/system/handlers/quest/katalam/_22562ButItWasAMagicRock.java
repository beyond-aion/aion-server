package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
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
 * @author Cheatkiller, Modified by Artur
 */
public class _22562ButItWasAMagicRock extends QuestHandler {

	private final static int questId = 22562;

	public _22562ButItWasAMagicRock() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(800565).addOnQuestStart(questId); // Othur.
		qe.registerQuestNpc(800565).addOnTalkEvent(questId); // Othur.
		qe.registerQuestItem(182213362, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 800565) { // Othur.
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182213362, 1);
				}
			}

		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 800565) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				}
				if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					changeQuestStep(env, 1, 2, true);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800565) { // Othur.
				removeQuestItem(env, 182213362, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (id != 182213362)
			return HandlerResult.UNKNOWN;

		if (qs == null || qs.getStatus() != QuestStatus.START)
			return HandlerResult.UNKNOWN;

		if (qs.getQuestVarById(0) != 0)
			return HandlerResult.UNKNOWN;

		if (!player.isInsideZone(ZoneName.get("LDF5A_ITEMUSEAREA_Q12562")))
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				changeQuestStep(env, 0, 1, false);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}

}
