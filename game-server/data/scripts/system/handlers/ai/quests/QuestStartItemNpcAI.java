package ai.quests;

import java.util.Set;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("quest_start_use_item")
public class QuestStartItemNpcAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		Set<Integer> relatedQuests = QuestEngine.getInstance().getQuestNpc(getOwner().getNpcId()).getOnQuestStart();
		DialogAction action = relatedQuests.isEmpty() ? DialogAction.USE_OBJECT : DialogAction.QUEST_SELECT;
		if (!QuestEngine.getInstance().onDialog(new QuestEnv(getOwner(), player, 0, action.id())))
			if (getObjectTemplate().isDialogNpc()) // show default dialog
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
	}
}
