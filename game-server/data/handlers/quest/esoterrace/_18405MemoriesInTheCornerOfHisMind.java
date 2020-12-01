package quest.esoterrace;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
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
 * @author Vincas
 */
public class _18405MemoriesInTheCornerOfHisMind extends AbstractQuestHandler {

	private static final int npcDaidra = 799553, npcTillen = 799552;

	public _18405MemoriesInTheCornerOfHisMind() {
		super(18405);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcDaidra).addOnTalkEvent(questId);
		qe.registerQuestNpc(npcTillen).addOnTalkEvent(questId);
		qe.registerQuestItem(182215002, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();

		if (env.getTargetId() == 0 && env.getDialogActionId() == QUEST_ACCEPT_1) {
			QuestService.startQuest(env);
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
			return true;
		}

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (env.getTargetId()) {
				case npcDaidra:
					if (qs.getQuestVarById(0) == 0) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 1352);
						else if (env.getDialogActionId() == SETPRO1)
							return defaultCloseDialog(env, 0, 1, 182215024, 1, 182215002, 1);
					}
					return false;
				case npcTillen:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2375);
						else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
							removeQuestItem(env, 182215024, 1);
						return defaultCloseDialog(env, 1, 2, true, true);
					}
			}
		}
		return sendQuestRewardDialog(env, npcTillen, 0);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if (id != 182215002)
			return HandlerResult.FAILED;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				sendQuestDialog(env, 4);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}
}
