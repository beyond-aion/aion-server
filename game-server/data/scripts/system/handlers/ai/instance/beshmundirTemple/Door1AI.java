package ai.instance.beshmundirTemple;

import java.util.Arrays;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Tibald, Neon
 */
@AIName("door1")
public class Door1AI extends ActionItemNpcAI {

	public Door1AI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		int questId = player.getRace() == Race.ELYOS ? 30208 : 30308;
		// Only one player in group has to have this quest
		for (Player member : player.isInGroup() ? player.getPlayerGroup().getOnlineMembers() : Arrays.asList(player)) {
			if (member.getQuestStateList().hasQuest(questId)) {
				super.handleDialogStart(player);
				return;
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		AIActions.deleteOwner(this);
	}
}
