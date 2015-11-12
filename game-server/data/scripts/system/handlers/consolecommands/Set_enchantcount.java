package consolecommands;

import java.util.HashMap;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Set_enchantcount extends ConsoleCommand {

	public Set_enchantcount() {
		super("set_enchantcount");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		int objId;
		int enchant;
		@SuppressWarnings("unused")
		int unk;

		try {
			objId = Integer.parseInt(params[0]);
			enchant = Integer.parseInt(params[1]);
			unk = Integer.parseInt(params[2]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Invalid params.");
			return;
		}

		Item item = admin.getInventory().getItemByObjId(objId);
		if (item == null)
			item = admin.getEquipment().getEquippedItemByObjId(objId);

		if (item != null) {
			int curEnchant = item.getEnchantLevel();
			int maxEnchant = item.getItemTemplate().getMaxEnchantLevel();
			maxEnchant += item.getItemTemplate().getMaxEnchantBonus();

			if (curEnchant + enchant <= maxEnchant) {
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
					HashMap<Integer, List<EnchantStat>> enc = DataManager.ENCHANT_DATA.getTemplates(item.getItemTemplate().getItemGroup());
					if (enc != null) {
						item.setEnchantEffect(new EnchantEffect(item, admin, enc.get(item.getEnchantLevel())));
					}
				}
				if (item.isEquipped())
					admin.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
				else
					admin.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);

				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEED_NEW(new DescriptionId(item.getNameId()), enchant));
			} else {
				PacketSendUtility.sendMessage(admin, "Max Enchat for this item is: " + maxEnchant);
			}
		} else {
			PacketSendUtility.sendMessage(admin, "Invalid item.");
			return;
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///set_enchantcount <item Id> <enchantcount> <unk>");
	}
}
