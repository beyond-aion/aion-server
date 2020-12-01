package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Gigi
 */
@AIName("door3")
public class Door3AI extends ActionItemNpcAI {

	public Door3AI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getInventory().getItemCountByItemId(185000091) > 0) {
			super.handleDialogStart(player);
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		AIActions.deleteOwner(this);
	}
}
