package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Gigi
 */
@AIName("door3")
public class Door3AI extends ActionItemNpcAI {

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getInventory().getItemCountByItemId(185000091) > 0) {
			super.handleDialogStart(player);
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		AIActions.deleteOwner(this);
	}
}
