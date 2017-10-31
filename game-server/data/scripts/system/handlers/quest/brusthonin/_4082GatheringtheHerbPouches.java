package quest.brusthonin;

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
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Nephis
 */
public class _4082GatheringtheHerbPouches extends AbstractQuestHandler {

	public _4082GatheringtheHerbPouches() {
		super(4082);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205190).addOnQuestStart(questId);
		qe.registerQuestNpc(205190).addOnTalkEvent(questId);
		qe.registerQuestNpc(700430).addOnTalkEvent(questId);
		qe.registerQuestNpc(700431).addOnTalkEvent(questId);
		qe.registerQuestNpc(700432).addOnTalkEvent(questId);
		qe.registerQuestItem(182209058, questId);
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if (id != 182209058 || qs == null)
			return HandlerResult.UNKNOWN;

		if (qs.getQuestVarById(0) != 0)
			return HandlerResult.FAILED;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 205190) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182209058, 1))
						return sendQuestStartDialog(env);
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM) {
					if (QuestService.collectItemCheck(env, true)) {
						removeQuestItem(env, 182209058, 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 2716);
				} else
					return sendQuestEndDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD)
				return sendQuestEndDialog(env);
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700430:
				case 700431:
				case 700432:
					if (qs.getQuestVarById(0) == 0 && env.getDialogActionId() == USE_OBJECT)
						return true;
			}
		}
		return false;
	}
}
