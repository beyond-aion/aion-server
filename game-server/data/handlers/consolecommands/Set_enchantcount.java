package consolecommands;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Set_enchantcount extends ConsoleCommand {

	public Set_enchantcount() {
		super("set_enchantcount", "Enchants an item.", """
			<item object id> <levels> - Adds enchant levels to the item in your inventory or equipment.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		int objId = Integer.parseInt(params[0]);
		int enchant = Integer.parseInt(params[1]);

		Item item = admin.getInventory().getItemByObjId(objId);
		if (item == null)
			item = admin.getEquipment().getEquippedItemByObjId(objId);

		if (item != null) {
			int curEnchant = item.getEnchantLevel();
			int maxEnchant = item.getItemTemplate().getMaxEnchantLevel();
			maxEnchant += item.getItemTemplate().getMaxEnchantBonus();

			if (curEnchant + enchant < 0) {
				sendInfo(admin, "The enchant level cannot go below 0, it is " + curEnchant + ".");
			} else if (curEnchant + enchant <= maxEnchant) {
				curEnchant += enchant;
				item.setEnchantLevel(Math.min(curEnchant, maxEnchant));
				if (item.isEquipped())
					admin.getGameStats().updateStatsVisually();

				ItemPacketService.updateItemAfterInfoChange(admin, item, ItemUpdateType.STATS_CHANGE);
				if (item.getEnchantEffect() != null) {
					item.getEnchantEffect().endEffect(admin);
					item.setEnchantEffect(null);
				}

				if (item.getEnchantLevel() > 0 && item.isEquipped()) {
					EnchantService.applyEnchantEffect(item, admin, item.getEnchantLevel());
				}
				if (item.isEquipped())
					admin.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
				else
					admin.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);

				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEED_NEW(item.getL10n(), enchant));
			} else {
				sendInfo(admin, "The max enchant level of this item is " + maxEnchant);
			}
		} else {
			sendInfo(admin, "There is no item with this object ID in your inventory or equipment.");
		}
	}
}
