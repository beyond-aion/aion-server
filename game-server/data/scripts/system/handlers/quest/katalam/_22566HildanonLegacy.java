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
 * @author cheatkiller, Modified by Artur
 */
public class _22566HildanonLegacy extends QuestHandler {

	private final static int questId = 22566;

	public _22566HildanonLegacy() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(800556).addOnQuestStart(questId); // Pahel.
		qe.registerQuestNpc(800556).addOnTalkEvent(questId); // Pahel.
		qe.registerQuestNpc(800556).addOnTalkEvent(questId); // Pahel.
		qe.registerOnEnterZone(ZoneName.get("LDF5A_SENSORYAREA_Q22566_206323_11_600050000"), questId);
		qe.registerQuestItem(182213365, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 800556) { // Pahel.
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182213365, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800556) { // Pahel.
				removeQuestItem(env, 182213365, 1);
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

		if (id != 182213365)
			return HandlerResult.UNKNOWN;

		if (qs == null || qs.getStatus() != QuestStatus.START)
			return HandlerResult.UNKNOWN;

		if (qs.getQuestVarById(0) != 1)
			return HandlerResult.UNKNOWN;

		if (!player.isInsideZone(ZoneName.get("LDF5A_SENSORYAREA_Q22566_206323_11_600050000")))
			return HandlerResult.UNKNOWN;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				changeQuestStep(env, 1, 2, true);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("LDF5A_SENSORYAREA_Q22566_206323_11_600050000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 0) {
					changeQuestStep(env, 0, 1, false);
					return true;
				}
			}
		}
		return false;
	}
}
