package ai.instance.dredgion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author cheatkiller
 */
@AIName("shulackdrudge")
public class ShulackDrudgeAI extends GeneralNpcAI {

	public ShulackDrudgeAI(Npc owner) {
		super(owner);
	}

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
			getOwner().overrideNpcType(CreatureType.PEACE);
		}
	}
}
