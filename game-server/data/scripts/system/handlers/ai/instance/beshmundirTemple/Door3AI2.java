package ai.instance.beshmundirTemple;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 *
 * @author Gigi
 */
@AIName("door3")
public class Door3AI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getInventory().getItemCountByItemId(185000091) > 0) {
			super.handleDialogStart(player);
		}
		else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		AI2Actions.deleteOwner(this);
	}
}
