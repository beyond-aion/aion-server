package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * This class is responsible for armsfusion related tasks (fusion and breaking, called COMPOUND and DECOMPOUND by the client)
 * 
 * @author Wakizashi, Source, xTz, Neon
 */
public class ArmsfusionService {

	public static void fusionWeapons(Player player, int mainWeaponObjId, int fuseWeaponObjId) {
		Item mainWeapon = player.getInventory().getItemByObjId(mainWeaponObjId);
		Item fuseWeapon = player.getInventory().getItemByObjId(fuseWeaponObjId);

		// Check if item is in bag
		if (mainWeapon == null || fuseWeapon == null) {
			if (player.getEquipment().getEquippedItemByObjId(mainWeaponObjId) != null
				|| player.getEquipment().getEquippedItemByObjId(fuseWeaponObjId) != null)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_EQUIPED_ITEM());
			else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ITEM_NO_TARGET_ITEM());
				AuditLogger.log(player, "tried to fuse weapons he doesn't have (obj IDs:" + mainWeaponObjId + ", " + fuseWeaponObjId + ")");
			}
			return;
		}

		if (!mainWeapon.getItemTemplate().isCanFuse() || !fuseWeapon.getItemTemplate().isCanFuse()) {
			Item item = mainWeapon.getItemTemplate().isCanFuse() ? mainWeapon : fuseWeapon;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_AVAILABLE(item.getL10n()));
			AuditLogger.log(player,
				"tried to fuse item " + fuseWeapon.getItemId() + " onto " + mainWeapon.getItemId() + " (" + item.getItemId() + " isn't fusible)");
			return;
		}

		long basePricePerLevelSquared = getBasePricePerLevelSquared(mainWeapon.getItemTemplate().getItemQuality());
		int level = mainWeapon.getItemTemplate().getLevel();
		long price = PricesService.getPriceForService(basePricePerLevelSquared * level * level, player.getRace());

		if (player.getInventory().getKinah() < price) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_ENOUGH_MONEY(mainWeapon.getL10n(), fuseWeapon.getL10n()));
			return;
		}

		if (mainWeapon.getTemporaryExchangeTime() != 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_TEMPORARY_EXCHANGE_ITEM());
			return;
		}

		// Fusioned weapons must be not fusioned
		if (mainWeapon.hasFusionedItem() || fuseWeapon.hasFusionedItem()) {
			Item item = mainWeapon.hasFusionedItem() ? mainWeapon : fuseWeapon;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_AVAILABLE(item.getL10n()));
			return;
		}

		// Fusioned weapons must have same type
		if (mainWeapon.getItemTemplate().getItemGroup() != fuseWeapon.getItemTemplate().getItemGroup()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_DIFFERENT_TYPE());
			return;
		}

		// Second weapon must have inferior or equal lvl. in relation to first weapon
		if (fuseWeapon.getItemTemplate().getLevel() > mainWeapon.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_MAIN_REQUIRE_HIGHER_LEVEL());
			return;
		}

		// You can not combine Conditioning and Augmenting
		if (mainWeapon.getImprovement() != null && fuseWeapon.getImprovement() != null) {
			if (mainWeapon.getImprovement().getChargeWay() != fuseWeapon.getImprovement().getChargeWay()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_COMPARABLE_ITEM());
				return;
			}
		}

		if (!player.getInventory().decreaseByObjectId(fuseWeaponObjId, 1))
			return;
		mainWeapon.setFusionedItem(fuseWeapon);
		ItemSocketService.copyFusionStones(fuseWeapon, mainWeapon);
		mainWeapon.setPersistentState(PersistentState.UPDATE_REQUIRED);
		InventoryDAO.store(mainWeapon, player);

		ItemPacketService.updateItemAfterInfoChange(player, mainWeapon);
		player.getInventory().decreaseKinah(price);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_SUCCESS(mainWeapon.getL10n(), fuseWeapon.getL10n()));
	}

	private static long getBasePricePerLevelSquared(ItemQuality rarity) {
		switch (rarity) {
			case JUNK:
			case COMMON:
				return 200;
			case RARE:
				return 250;
			case LEGEND:
				return 300;
			case UNIQUE:
				return 400;
			case EPIC:
				return 500;
			case MYTHIC:
			default:
				return 600;
		}
	}

	public static void breakWeapons(Player player, int weaponToBreakUniqueId) {
		Item weaponToBreak = player.getInventory().getItemByObjId(weaponToBreakUniqueId);

		if (weaponToBreak == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOUND_ITEM_NO_TARGET_ITEM());
			return;
		}

		if (!weaponToBreak.hasFusionedItem()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOUND_ERROR_NOT_AVAILABLE(weaponToBreak.getL10n()));
			return;
		}

		weaponToBreak.setFusionedItem(null);
		InventoryDAO.store(weaponToBreak, player);

		ItemPacketService.updateItemAfterInfoChange(player, weaponToBreak);

		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUNDED_ITEM_DECOMPOUND_SUCCESS(weaponToBreak.getL10n()));
	}
}
