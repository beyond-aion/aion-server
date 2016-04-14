package ai.instance.sauroBase;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
@AIName("saurohiddenpassage")
public class SauroFinalTeleportAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		switchWay(player, dialogId);
		return true;
	}

	private void switchWay(Player player, int dialogId) {
		switch (DialogAction.getActionByDialogId(dialogId)) {
			case SELECT_BOSS_LEVEL3:
				checkKeys(player, 1);
				break;
			case SELECT_BOSS_LEVEL4:
				checkKeys(player, 2);
				break;
		}
	}

	private void checkKeys(Player player, long keyCount) {
		Item keys = player.getInventory().getFirstItemByItemId(185000179);
		int portal = (int) (730875 + keyCount);
		if (keys != null && keys.getItemCount() >= keyCount) {
			spawn(portal, 127.5f, 432.8f, 151, (byte) 119);
			player.getInventory().decreaseByItemId(185000179, keys.getItemCount());
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			AI2Actions.deleteOwner(this);
			PacketSendUtility.broadcastToMap(getOwner(), 1401922);
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352));
		}
	}
}
