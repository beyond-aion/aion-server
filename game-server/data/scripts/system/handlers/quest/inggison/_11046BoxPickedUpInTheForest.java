package quest.inggison;

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

/**
 * @author Dta3000
 */
public class _11046BoxPickedUpInTheForest extends QuestHandler {

	private final static int questId = 11046;

	public _11046BoxPickedUpInTheForest() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798954).addOnTalkEvent(questId);
		qe.registerQuestItem(182206745, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId == 0) {
			switch (env.getDialog()) {
				case QUEST_ACCEPT_1:
					QuestService.startQuest(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
				case QUEST_REFUSE_1:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
			}
		}
		else if (targetId == 798954) {
			if (qs != null) {
				if (env.getDialog() == DialogAction.QUEST_SELECT && qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 2375);
				}
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id() && qs.getStatus() != QuestStatus.COMPLETE
					&& qs.getStatus() != QuestStatus.NONE) {
					removeQuestItem(env, 182206745, 1);
					qs.setQuestVar(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				}
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (id != 182206745)
			return HandlerResult.UNKNOWN;
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
		if (qs == null || qs.getStatus() == QuestStatus.NONE)
			sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
