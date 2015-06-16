package com.aionemu.gameserver.services.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class ItemSocketService {

   private static final Logger log = LoggerFactory.getLogger(ItemSocketService.class);

   public static ManaStone addManaStone(Item item, int itemId) {
	  if (item == null)
		 return null;

	  int maxSlots = item.getSockets(false);
	  Set<ManaStone> manaStones = item.getItemStones();
	  if (manaStones.size() >= maxSlots)
		 return null;

	   ItemGroup manastoneCategory = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup();
	  int specialSlotCount = item.getItemTemplate().getSpecialSlots();
	  if (manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotCount == 0)
		 return null;

	  int specialSlotsOccupied = 0;
	  int normalSlotsOccupied = 0;
	  HashSet<Integer> allSlots = new HashSet<>();
	  for (ManaStone ms : manaStones) {
		  ItemGroup category = DataManager.ITEM_DATA.getItemTemplate(ms.getItemId()).getItemGroup();
		 if (category == ItemGroup.SPECIAL_MANASTONE)
			specialSlotsOccupied++;
		 else
			normalSlotsOccupied++;
		 allSlots.add(ms.getSlot());
	  }

	  if ((manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotsOccupied >= specialSlotCount)
			  || (manastoneCategory == ItemGroup.MANASTONE && normalSlotsOccupied >= (maxSlots - specialSlotCount)))
		 return null;

	  int start = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? 0 : specialSlotCount;
	  int end = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? specialSlotCount : maxSlots;
	  int nextSlot = start;
	  boolean slotFound = false;
	  for (; nextSlot < end; nextSlot++) {
		 if (!allSlots.contains(nextSlot)) {
			slotFound = true;
			break;
		 }
	  }
	  if (!slotFound)
		 return null;

	  ManaStone stone = new ManaStone(item.getObjectId(), itemId, nextSlot, PersistentState.NEW);
	  manaStones.add(stone);

	  return stone;
   }

   public static ManaStone addManaStone(Item item, int itemId, int slotId) {
	  if (item == null)
		 return null;

	  Set<ManaStone> manaStones = item.getItemStones();
	  if (manaStones.size() >= Item.MAX_BASIC_STONES)
		 return null;

	  ManaStone stone = new ManaStone(item.getObjectId(), itemId, slotId, PersistentState.NEW);
	  manaStones.add(stone);
	  return stone;
   }

   public static void copyFusionStones(Item source, Item target) {
	  if (source.hasManaStones()) {
		 for (ManaStone manaStone : source.getItemStones()) {
			target.getFusionStones().add(
					new ManaStone(target.getObjectId(), manaStone.getItemId(), manaStone.getSlot(), PersistentState.NEW));
		 }
	  }
   }

   public static ManaStone addFusionStone(Item item, int itemId) {
	  if (item == null)
		 return null;

	  int maxSlots = item.getSockets(true);
	  Set<ManaStone> manaStones = item.getFusionStones();
	  if (manaStones.size() >= maxSlots)
		 return null;

	  ItemGroup manastoneCategory = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup();
	  int specialSlotCount = item.getFusionedItemTemplate().getSpecialSlots();
	  if (manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotCount == 0)
		 return null;

	  int specialSlotsOccupied = 0;
	  int normalSlotsOccupied = 0;
	  HashSet<Integer> allSlots = new HashSet<>();
	  for (ManaStone ms : manaStones) {
		 ItemGroup category = DataManager.ITEM_DATA.getItemTemplate(ms.getItemId()).getItemGroup();
		 if (category == ItemGroup.SPECIAL_MANASTONE)
			specialSlotsOccupied++;
		 else
			normalSlotsOccupied++;
		 allSlots.add(ms.getSlot());
	  }

	  if ((manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotsOccupied >= specialSlotCount)
			  || (manastoneCategory == ItemGroup.MANASTONE && normalSlotsOccupied >= (maxSlots - specialSlotCount)))
		 return null;

	  int start = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? 0 : specialSlotCount;
	  int end = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? specialSlotCount : maxSlots;
	  int nextSlot = start;
	  boolean slotFound = false;
	  for (; nextSlot < end; nextSlot++) {
		 if (!allSlots.contains(nextSlot)) {
			slotFound = true;
			break;
		 }
	  }
	  if (!slotFound)
		 return null;

	  ManaStone stone = new ManaStone(item.getObjectId(), itemId, nextSlot, PersistentState.NEW);
	  manaStones.add(stone);
	  return stone;
   }

   public static ManaStone addFusionStone(Item item, int itemId, int slotId) {
	  if (item == null)
		 return null;

	  Set<ManaStone> fusionStones = item.getFusionStones();
	  if (fusionStones.size() > item.getSockets(true))
		 return null;

	  ManaStone stone = new ManaStone(item.getObjectId(), itemId, slotId, PersistentState.NEW);
	  fusionStones.add(stone);
	  return stone;
   }

   public static void removeManastone(Player player, int itemObjId, int slotNum) {
	  Storage inventory = player.getInventory();
	  Item item = inventory.getItemByObjId(itemObjId);
	  long price = PricesService.getPriceForService(650, player.getRace());
	  
	  if (player.getInventory().getKinah() < price) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NOT_ENOUGH_GOLD(new DescriptionId(item.getNameId())));
		 return;
	  }

	  if (item == null) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_TARGET_ITEM);
		 return;
	  }

	  if (!item.hasManaStones()) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_OPTION_TO_REMOVE(new DescriptionId(item.getNameId())));
		 log.warn("Item stone list is empty");
		 return;
	  }

	  Set<ManaStone> itemStones = item.getItemStones();

	  boolean found = false;
	  for (ManaStone ms : itemStones) {
		 if (ms.getSlot() == slotNum) {
			ms.setPersistentState(PersistentState.DELETED);
			DAOManager.getDAO(ItemStoneListDAO.class).storeManaStones(Collections.singleton(ms));
			itemStones.remove(ms);
			found = true;
			break;
		 }
	  }
	  if (!found) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_INVALID_OPTION_SLOT_NUMBER(new DescriptionId(item.getNameId())));
		 log.warn("Invalid slot ID at manastone removal!");
		 return;
	  }
	  player.getInventory().decreaseKinah(price);
	  PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_SUCCEED(new DescriptionId(item.getNameId())));
	  ItemPacketService.updateItemAfterInfoChange(player, item);
   }

   public static void removeFusionstone(Player player, int itemObjId, int slotNum) {

	  Storage inventory = player.getInventory();
	  Item item = inventory.getItemByObjId(itemObjId);
	  long price = PricesService.getPriceForService(650, player.getRace());
	  if (player.getInventory().getKinah() < price) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NOT_ENOUGH_GOLD(new DescriptionId(item.getNameId())));
		 return;
	  }

	  if (item == null) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_TARGET_ITEM);
		 return;
	  }

	  if (!item.hasFusionStones()) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_OPTION_TO_REMOVE(new DescriptionId(item.getNameId())));
		 log.warn("Item stone list is empty");
		 return;
	  }

	  Set<ManaStone> itemStones = item.getFusionStones();

	  boolean found = false;
	  for (ManaStone ms : itemStones) {
		 if (ms.getSlot() == slotNum) {
			ms.setPersistentState(PersistentState.DELETED);
			DAOManager.getDAO(ItemStoneListDAO.class).storeFusionStone(Collections.singleton(ms));
			itemStones.remove(ms);
			found = true;
			break;
		 }
	  }
	  if (!found) {
		 PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_INVALID_OPTION_SLOT_NUMBER(new DescriptionId(item.getNameId())));
		 log.warn("Invalid slot ID at manastone removal!");
		 return;
	  }
	  player.getInventory().decreaseKinah(price);
	  PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_SUCCEED(new DescriptionId(item.getNameId())));
	  ItemPacketService.updateItemAfterInfoChange(player, item);
   }

   public static void removeAllManastone(Player player, Item item) {
	  if (item == null) {
		 log.warn("Item not found during manastone remove");
		 return;
	  }

	  if (!item.hasManaStones()) {
		 return;
	  }

	  Set<ManaStone> itemStones = item.getItemStones();
	  for (ManaStone ms : itemStones) {
		 ms.setPersistentState(PersistentState.DELETED);
	  }
	  DAOManager.getDAO(ItemStoneListDAO.class).storeManaStones(itemStones);
	  itemStones.clear();

	  ItemPacketService.updateItemAfterInfoChange(player, item);
   }

   public static void removeAllFusionStone(Player player, Item item) {
	  if (item == null) {
		 log.warn("Item not found during manastone remove");
		 return;
	  }

	  if (!item.hasFusionStones()) {
		 return;
	  }

	  Set<ManaStone> fusionStones = item.getFusionStones();
	  for (ManaStone ms : fusionStones) {
		 ms.setPersistentState(PersistentState.DELETED);
	  }
	  DAOManager.getDAO(ItemStoneListDAO.class).storeFusionStone(fusionStones);
	  fusionStones.clear();

	  ItemPacketService.updateItemAfterInfoChange(player, item);
   }

	public static void socketGodstone(Player player, int weaponId, int stoneId) {
		Item weaponItem = player.getInventory().getItemByObjId(weaponId);
		if (weaponItem == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_CANNOT_GIVE_PROC_TO_EQUIPPED_ITEM);
			return;
		}

		if (!weaponItem.canSocketGodstone()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NOT_ADD_PROC(new DescriptionId(weaponItem.getNameId())));
			AuditLogger.info(player, "Player try insert godstone in not compatible item");
			return;
		}

		final StartMovingListener move = new StartMovingListener() {
			@Override
			public void moved() {
				super.moved();
				player.getObserveController().removeObserver(this);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402238, new DescriptionId(weaponItem.getNameId())));
				if (player.getMoveController().isInMove()) {
					player.getMoveController().abortMove();
					player.getController().cancelUseItem();
				}
			}
		};

		player.getObserveController().attach(move);
		
		Item godstone = player.getInventory().getItemByObjId(stoneId);

		int godStoneItemId = godstone.getItemTemplate().getTemplateId();
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(godStoneItemId);
		GodstoneInfo godstoneInfo = itemTemplate.getGodstoneInfo();

		if (godstoneInfo == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_PROC_GIVE_ITEM);
			log.warn("Godstone info missing for itemid " + godStoneItemId);
			return;
		}

		PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), stoneId,
				itemTemplate.getTemplateId(), 2000, 0, 0));

		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				player.getObserveController().removeObserver(move);

				boolean isSuccess = true;

				//small change to fail???
				//if (Rnd.get(1, 100) > 20)
					//isSuccess = false;
				
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), stoneId,
						itemTemplate.getTemplateId(), 0, isSuccess ? 1 : 2, 0));

				if (!player.getInventory().decreaseByObjectId(stoneId, 1))
					return;
				
				if(isSuccess){
					weaponItem.addGodStone(godStoneItemId);
					PacketSendUtility.sendPacket(player,
							SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_ENCHANTED_TARGET_ITEM(new DescriptionId(weaponItem.getNameId())));

					ItemPacketService.updateItemAfterInfoChange(player, weaponItem);
				}
			}
		}, 2000));
	}
}
