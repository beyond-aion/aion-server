package ai.instance.dredgion;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI2;

/**
 * @author cheatkiller
 */
@AIName("shulackdrudge")
public class ShulackDrudgeAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleDialogFinish(Player player) {
		addItems(player);
		super.handleDialogFinish(player);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	private void addItems(Player player) {
		int itemId = player.getRace() == Race.ELYOS ? 182212606 : 182212607;
		Item dredgionSupplies = player.getInventory().getFirstItemByItemId(itemId);
		if (dredgionSupplies == null) {
			ItemService.addItem(player, itemId, 1);
			getOwner().setNpcType(CreatureType.PEACE);
			getKnownList().forEachPlayer(new Consumer<Player>() {

				@Override
				public void accept(Player player) {
					PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(getOwner().getObjectId(), 0, getOwner().getType(player).getId(), 0));
				}
			});
		}
	}
}
