package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.TamperingAction;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Tago, Wakizashi
 */
public class Equip extends AdminCommand {

	public Equip() {
		super("equip", "Enchants all equipped items.");
		// @formatter:off
		setSyntaxInfo(
			"socket <manastone link|ID> [limit] [player] - Sockets the manastone in all equipped items of your target or the given player.",
			"unsocket [player] - Removes manastones from all equipped items of your target or the given player.",
			"enchant <0-255> [player] - Enchant all equipped items of your target or the given player.",
			"temper <0-255> [player] - Temper all equipped items of your target or the given player."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		Player player = World.getInstance().getPlayer(Util.convertName(params[params.length - 1]));
		if (player == null)
			player = admin.getTarget() instanceof Player target ? target : admin;
		if ("socket".equalsIgnoreCase(params[0]) && params.length >= 2) {
			ItemTemplate manastone = DataManager.ITEM_DATA.getItemTemplate(ChatUtil.getItemId(params[1]));
			int count = params.length < 3 ? Integer.MAX_VALUE : Integer.parseInt(params[2]);
			socket(admin, player, manastone, count);
		} else if ("unsocket".equalsIgnoreCase(params[0])) {
			unsocket(admin, player);
		} else if ("enchant".equalsIgnoreCase(params[0]) && params.length >= 2) {
			int enchant = Math.max(0, Math.min(255, Integer.parseInt(params[1])));
			enchant(admin, player, enchant);
		} else if ("temper".equalsIgnoreCase(params[0]) && params.length >= 2) {
			int temperingLevel =  Math.max(0, Math.min(255, Integer.parseInt(params[1])));
			temper(admin, player, temperingLevel);
		} else {
			sendInfo(admin);
		}
	}

	private void socket(Player admin, Player player, ItemTemplate manastone, int count) {
		if (count <= 0) {
			sendInfo(admin, "Count must be greater than 0.");
			return;
		}
		if (manastone == null || manastone.getItemGroup() != ItemGroup.MANASTONE && manastone.getItemGroup() != ItemGroup.SPECIAL_MANASTONE) {
			sendInfo(admin, "Invalid manastone.");
			return;
		}
		int manastoneId = manastone.getTemplateId();
		int maxSocketed = 0;
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (targetItem.getSockets(false) == 0)
				continue;
			for (int counter = 0; counter < count;) {
				ManaStone manaStone = ItemSocketService.addManaStone(targetItem, manastoneId, false);
				if (manaStone == null)
					break;
				maxSocketed = Math.max(maxSocketed, ++counter);
				ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
			}
			if (maxSocketed > 0) {
				ItemPacketService.updateItemAfterInfoChange(player, targetItem);
				targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
			}
		}
		player.getGameStats().updateStatsVisually();
		if (maxSocketed == 0)
			sendInfo(admin, "There are no free slots on any equipped items.");
		else if (player == admin)
			sendInfo(player, maxSocketed + "x " + ChatUtil.item(manastoneId) + " were added to free slots on all equipped items");
		else {
			sendInfo(admin, maxSocketed + "x " + ChatUtil.item(manastoneId) + " were added to free slots on all equipped items of player " + player.getName());
			sendInfo(player, admin.getName(true) + " added " + count + "x " + ChatUtil.item(manastoneId) + " to free slots on all your equipped items");
		}
	}

	private void unsocket(Player admin, Player player) {
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (targetItem.getItemStonesSize() > 0) {
				ItemEquipmentListener.removeStoneStats(targetItem.getItemStones(), player.getGameStats());
				ItemSocketService.removeAllManastone(player, targetItem);
				ItemPacketService.updateItemAfterInfoChange(player, targetItem);
				targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
			}
		}
		player.getGameStats().updateStatsVisually();
		if (player == admin)
			sendInfo(player, "Removed manastones from all equipped items.");
		else {
			sendInfo(admin, "Removed manastones from all equipped items of player " + player.getName() + ".");
			sendInfo(player, admin.getName(true) + " removed all manastones from all your equipped items.");
		}
	}

	private void enchant(Player admin, Player player, int enchant) {
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (targetItem.getItemTemplate().isNoEnchant())
				continue;
			if (targetItem.getItemTemplate().getMaxEnchantLevel() == 0 && !targetItem.getItemTemplate().canExceedEnchant())
				continue;
			targetItem.setAmplified(enchant > targetItem.getItemTemplate().getMaxEnchantLevel() + targetItem.getEnchantBonus());
			EnchantService.setEnchantLevel(player, targetItem, enchant);
		}
		player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (player == admin)
			sendInfo(player, "Enchanted all equipped items to +" + enchant + ".");
		else {
			sendInfo(admin, "Enchanted all equipped items of player " + player.getName() + " to +" + enchant + ".");
			sendInfo(player, admin.getName(true) + " enchanted all your equipped items to +" + enchant + ".");
		}
	}

	private void temper(Player admin, Player player, int temperingLevel) {
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (targetItem.getItemTemplate().getMaxTampering() > 0)
				TamperingAction.setTemperingLevel(targetItem, player, Math.min(temperingLevel, targetItem.getItemTemplate().getMaxTampering()));
		}
		player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (player == admin)
			sendInfo(player, "Tempered all equipped items to +" + temperingLevel + ".");
		else {
			sendInfo(admin, "Tempered all equipped items of player " + player.getName() + " to +" + temperingLevel + ".");
			sendInfo(player, admin.getName(true) + " tempered all your equipped items to +" + temperingLevel + ".");
		}
	}
}
