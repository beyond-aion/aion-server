package ai.quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.Set;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
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

	public QuestStartItemNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		Set<Integer> relatedQuests = QuestEngine.getInstance().getQuestNpc(getOwner().getNpcId()).getOnQuestStart();
		if (!QuestEngine.getInstance().onDialog(new QuestEnv(getOwner(), player, 0, relatedQuests.isEmpty() ? USE_OBJECT : QUEST_SELECT)))
			if (getObjectTemplate().isDialogNpc()) // show default dialog
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
