package ai.quests;

import java.util.HashSet;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Actions.SelectDialogResult;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
@AIName("quest_start_use_item")
public class QuestStartItemNpcAi2 extends ActionItemNpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		HashSet<Integer> relatedQuests = QuestEngine.getInstance().getQuestNpc(getOwner().getNpcId()).getOnQuestStart();
		int dialogId = relatedQuests.isEmpty() ? DialogAction.USE_OBJECT.id() : DialogAction.QUEST_SELECT.id();
		SelectDialogResult dialogResult = AI2Actions.selectDialog(this, player, 0, dialogId);
		if (!dialogResult.isSuccess()) {
			if (isDialogNpc()) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
			}
			return;
		}
	}

	private boolean isDialogNpc() {
		return getObjectTemplate().isDialogNpc();
	}
}
