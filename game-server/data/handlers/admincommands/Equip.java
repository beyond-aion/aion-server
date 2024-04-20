package admincommands;

import java.lang.reflect.Field;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.ItemType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Tago, Wakizashi
 */
public class Equip extends AdminCommand {

	public Equip() {
		super("equip");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length != 0) {
			int i = 0;
			if ("help".startsWith(params[i])) {
				if (params[i + 1] == null)
					showHelp(admin);
				else if ("socket".startsWith(params[i + 1]))
					showHelpSocket(admin);
				else if ("enchant".startsWith(params[i + 1]))
					showHelpEnchant(admin);
				else if ("godstone".startsWith(params[i + 1]))
					showHelpGodstone(admin);
				return;
			}
			Player player = null;
			player = World.getInstance().getPlayer(Util.convertName(params[i]));
			if (player == null) {
				VisibleObject target = admin.getTarget();
				if (target instanceof Player)
					player = (Player) target;
				else
					player = admin;
			} else
				i++;
			if ("socket".startsWith(params[i])) {
				int manastone = 167000551;
				int quant = 0;
				try {
					manastone = params[i + 1] == null ? manastone : Integer.parseInt(params[i + 1]);
					quant = params[i + 2] == null ? quant : Integer.parseInt(params[i + 2]);
				} catch (Exception ex2) {
					showHelpSocket(admin);
					return;
				}
				socket(admin, player, manastone, quant);
				return;
			}
			if ("enchant".startsWith(params[i])) {
				int enchant = 0;
				try {
					enchant = params[i + 1] == null ? enchant : Integer.parseInt(params[i + 1]);
				} catch (Exception ex) {
					showHelpEnchant(admin);
					return;
				}
				enchant(admin, player, enchant);
				return;
			}
			if ("tamper".startsWith(params[i])) {
				int tampering = 0;
				try {
					tampering = params[i + 1] == null ? tampering : Integer.parseInt(params[i + 1]);
				} catch (Exception ex) {
					showHelpEnchant(admin);
					return;
				}
				tamper(admin, player, tampering);
				return;
			}
			if ("godstone".startsWith(params[i])) {
				int godstone = 100;
				try {
					godstone = params[i + 1] == null ? godstone : Integer.parseInt(params[i + 1]);
				} catch (Exception ex) {
					showHelpGodstone(admin);
					return;
				}
				godstone(admin, player, godstone);
				return;
			}
		}
		showHelp(admin);
	}

	private void socket(Player admin, Player player, int manastone, int quant) {
		if (manastone != 0 && (manastone < 167000000 || manastone > 168000000)) {
			sendInfo(admin, "You are suposed to give the item id for a" + " Manastone or 0 to remove all manastones.");
			return;
		}
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (isUpgradeble(targetItem)) {
				if (manastone == 0) {
					ItemEquipmentListener.removeStoneStats(targetItem.getItemStones(), player.getGameStats());
					ItemSocketService.removeAllManastone(player, targetItem);
				} else {
					int counter = quant <= 0 ? getMaxSlots(targetItem) : quant;
					while (targetItem.getItemStones().size() < getMaxSlots(targetItem) && counter >= 0) {
						ManaStone manaStone = ItemSocketService.addManaStone(targetItem, manastone, false);
						ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
						counter--;

					}
				}
				PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
				ItemPacketService.updateItemAfterInfoChange(player, targetItem);
				targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
			}

		}
		if (manastone == 0) {
			if (player == admin)
				sendInfo(player, "All Manastones removed from all equipped Items");
			else {
				sendInfo(admin, "All Manastones removed from all equipped Items by the Player " + player.getName());
				sendInfo(player, "Admin " + admin.getName() + " removed all manastones from all your equipped Items");
			}
		} else {
			if (player == admin)
				sendInfo(player, quant + "x [item: " + manastone + "] were added to free slots on all equipped items");
			else {
				sendInfo(admin, quant + "x [item: " + manastone + "] were added to free slots on all equipped items by the Player " + player.getName());
				sendInfo(player, "Admin " + admin.getName() + " added " + quant + "x [item: " + manastone + "] to free slots on all your equipped items");
			}
		}
	}

	private void godstone(Player admin, Player player, int godstone) {
		Item targetItem = player.getEquipment().getMainHandWeapon();
		if (godstone > 100000000) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(godstone);
			GodstoneInfo godstoneInfo = itemTemplate.getGodstoneInfo();
			if (godstoneInfo == null) {
				sendInfo(admin, "Wrong GodStone ItemID");
				return;
			}
			targetItem.addGodStone(godstone);
			PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
			ItemPacketService.updateItemAfterInfoChange(player, targetItem);
			targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
			if (player == admin)
				sendInfo(player, "[Item: " + godstone + "] socketed to your equipped MainHandWeapon [Item: " + targetItem.getItemId() + "]");
			else {
				sendInfo(admin,
					"[Item: " + godstone + "] socketed to the Player " + player.getName() + "equipped MainHandWeapon [Item: " + targetItem.getItemId() + "]");
				sendInfo(player,
					"Admin " + admin.getName() + " socketed [Item: " + godstone + "]  to you equipped MainHandWeapon [Item: " + targetItem.getItemId() + "]");
			}
		} else if (targetItem.getGodStone() != null) {
			try {
				if (godstone <= 100)
					godstone *= 10;
				if (godstone > 1000)
					godstone = 1000;
				Class<?> gs = targetItem.getGodStone().getClass();
				Field probability = gs.getDeclaredField("probability");
				Field probabilityLeft = gs.getDeclaredField("probability");
				probability.setAccessible(true);
				probabilityLeft.setAccessible(true);
				probability.setInt(targetItem.getGodStone(), godstone);
				probabilityLeft.setInt(targetItem.getGodStone(), godstone);
			} catch (Exception ex2) {
				sendInfo(admin, "Occurs an error.");
				return;
			}
			if (player.equals(admin))
				sendInfo(player, "Your godstone on your MainHandWeapon will now activate around " + (godstone / 10) + " percent of the time.");
			else {
				sendInfo(admin, "Player " + player.getName() + " godstone on MainHandWeapon will now activate around " + godstone + " percent of the time.");
				sendInfo(player,
					"Admin " + admin.getName() + " blessed your godstone on your MainHandWeapon to now activate around " + godstone + " percent of the time.");
			}
		}
	}

	private void enchant(Player admin, Player player, int enchant) {
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (isUpgradeble(targetItem)) {
				if (targetItem.getEnchantLevel() == enchant)
					continue;
				if (enchant > 255)
					enchant = 255;
				if (enchant < 0)
					enchant = 0;

				targetItem.setEnchantLevel(enchant);
				if (targetItem.isEquipped()) {
					player.getGameStats().updateStatsVisually();
				}
				ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);
			}
		}
		if (player == admin)
			sendInfo(player, "All equipped items were enchanted to level " + enchant);
		else {
			sendInfo(admin, "All equipped items by the Player " + player.getName() + " were enchanted to " + enchant);
			sendInfo(player, "Admin " + admin.getName() + " enchanted all your equipped items to level " + enchant);
		}

	}

	private void tamper(Player admin, Player player, int tampering) {
		for (Item targetItem : player.getEquipment().getEquippedItemsWithoutStigma()) {
			if (isTampering(targetItem)) {
				if (targetItem.getTempering() == tampering)
					continue;
				if (tampering > 255)
					tampering = 255;
				if (tampering < 0)
					tampering = 0;

				targetItem.setTempering(tampering);
				if (targetItem.isEquipped()) {
					player.getGameStats().updateStatsVisually();
				}
				ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);
			}
		}
		if (player == admin)
			sendInfo(player, "All equipped items were tampering to level " + tampering);
		else {
			sendInfo(admin, "All equipped items by the Player " + player.getName() + " were tampering to " + tampering);
			sendInfo(player, "Admin " + admin.getName() + " tampering all your equipped items to level " + tampering);
		}

	}

	/**
	 * Verify if the item is enchantble and/or socketble
	 */
	public static boolean isUpgradeble(Item item) {
		if (item.getItemTemplate().isNoEnchant())
			return false;
		if (item.getItemTemplate().isWeapon())
			return true;
		if (item.getItemTemplate().isArmor()) {
			long at = item.getItemTemplate().getItemSlot();
			if (at == 1 || /* Main Hand */
				at == 2 || /* Sub Hand */
				at == 8 || /* Jacket */
				at == 16 || /* Gloves */
				at == 32 || /* Boots */
				at == 2048 || /* Shoulder */
				at == 4096 || /* Pants */
				at == 131072 || /* Main Off Hand */
				at == 262144) /* Sub Off Hand */
				return true;
		}
		return false;
	}

	public static boolean isTampering(Item item) {
		if (item.getItemTemplate().getItemGroup() == ItemGroup.EARRING)
			return true;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.RING)
			return true;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.NECKLACE)
			return true;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.BELT)
			return true;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.HEAD)
			return true;
		if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME)
			return true;
		return false;
	}

	/**
	 * Returns the max number of manastones that can be socketed
	 */
	public static int getMaxSlots(Item item) {
		int slots = 0;
		switch (item.getItemTemplate().getItemQuality()) {
			case COMMON:
			case JUNK:
				slots = 1;
				break;
			case RARE:
				slots = 2;
				break;
			case LEGEND:
				slots = 3;
				break;
			case UNIQUE:
				slots = 4;
				break;
			case EPIC:
				slots = 5;
				break;
			case MYTHIC:
				slots = 5;
				break;
			default:
				slots = 0;
				break;
		}
		if (item.getItemTemplate().getItemType() == ItemType.DRACONIC)
			slots += 1;
		if (item.getItemTemplate().getItemType() == ItemType.ABYSS)
			slots += 2;
		return slots;

	}

	private void showHelp(Player admin) {
		sendInfo(admin,
			"[Help: Equip Command]\n" + "  Use //equip help <socket|enchant|godstone> for more details on the command.\n"
				+ "  Notice: This command uses smart matching. You may abbreviate most commands.\n"
				+ "  For example: (//equip so 167000551 5) will match to (//equip socket 167000551 5)");
	}

	private void showHelpEnchant(Player admin) {
		sendInfo(admin,
			"Syntax:  //equip [playerName] enchant [EnchantLevel = 0]\n" + "  This command Enchants all items equipped up to 255.\n"
				+ "  Notice: You can ommit parameters between [], especially playerName.\n"
				+ "  Target: Named player, then targeted player, only then self.\n" + "  Default Value: EnchantLevel is 0.");
	}

	private void showHelpSocket(Player admin) {
		sendInfo(admin, "Syntax:  //equip [playerName] socket [ManastoneID = 167000551] [Quantity = 0]\n"
			+ "  This command Sockets all free slots on equipped items, with the given manastone id.\n"
			+ "  Use ManastoneID = 0 to remove all Manastones. Quantity = 0 means to fill all free slots.\n"
			+ "  Notice: You can ommit parameters between [], especially playerName.\n" + "  Target: Named player, then targeted player, only then self.\n"
			+ "  Default Value: ManastoneID is 167000551, Quantity is 0 meaning fill all slots.");
	}

	private void showHelpGodstone(Player admin) {
		sendInfo(admin,
			"Syntax:  //equip [playerName] godstone [rate = 100|GodStoneID]\n"
				+ "  This command changes the godstone activation rate to the given number(0-100).\n"
				+ "  Give a GodStone ItemID and it will be socketed on you Main Hand Weapon.\n"
				+ "  Notice: You can ommit parameters between [], especially playerName.\n"
				+ "  Target: Named player, then targeted player, only then self.\n" + "  Default Value: Rate is 100 which is the default action .");
	}

	@Override
	public void info(Player player, String message) {
		showHelp(player);
	}
}
