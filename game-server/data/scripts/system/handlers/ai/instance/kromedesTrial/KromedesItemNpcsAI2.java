package ai.instance.kromedesTrial;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI2;

/**
 * @author Gigi, xTz
 */
@AIName("krobject")
public class KromedesItemNpcsAI2 extends ActionItemNpcAI2 {

	@Override
	public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 1012) {
			switch (getNpcId()) {
				case 730325:
					if (player.getInventory().getItemCountByItemId(164000142) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400701)); // TODO: more sys messages, but for
																																									// now not needed!
						ItemService.addItem(player, 164000142, 1);
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
					break;
				case 730340:
					if (player.getInventory().getItemCountByItemId(164000140) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400701)); // TODO: more sys messages, but for
																																									// now not needed!
						ItemService.addItem(player, 164000140, 1);
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
					break;
				case 730341:
					if (player.getInventory().getItemCountByItemId(164000143) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400701)); // TODO: more sys messages, but for
																																									// now not needed!
						ItemService.addItem(player, 164000143, 1);
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
					break;
			}
		}
		return true;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
