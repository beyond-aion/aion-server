package com.aionemu.gameserver.model.gameobjects.player;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.StigmaService;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Avol, ATracer, kosyachok, cura
 */
public class Equipment implements Persistable {

	private static final Logger log = LoggerFactory.getLogger(Equipment.class);

	private final SortedMap<Long, Item> equipment = Collections.synchronizedSortedMap(new TreeMap<>());
	private final Player owner;
	private PersistentState persistentState = PersistentState.UPDATED;

	private static final long[] ARMOR_SLOTS = new long[] { // @formatter:off
		ItemSlot.BOOTS.getSlotIdMask(),
		ItemSlot.GLOVES.getSlotIdMask(),
		ItemSlot.PANTS.getSlotIdMask(),
		ItemSlot.SHOULDER.getSlotIdMask(),
		ItemSlot.TORSO.getSlotIdMask()
	}; // @formatter:on

	public Equipment(Player player) {
		this.owner = player;
	}

	/**
	 * @return item or null in case of failure
	 */
	public Item equipItem(int itemUniqueId, long slot) {
		Item item = owner.getInventory().getItemByObjId(itemUniqueId);
		if (item == null || item.isEquipped())
			return null;

		ItemTemplate itemTemplate = item.getItemTemplate();
		if (itemTemplate.isTwoHandWeapon()) // client only sends main+sub slot when equipping via right click / double click
			slot = ItemSlot.MAIN_OR_SUB.getSlotIdMask();
		else if (itemTemplate.isOneHandWeapon() && !hasDualWieldingSkills())
			slot = ItemSlot.MAIN_HAND.getSlotIdMask();

		if (!itemTemplate.isClassSpecific(owner.getPlayerClass())) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_CLASS());
			return null;
		}
		// don't allow to wear items of not allowed level
		int requiredLevel = itemTemplate.getRequiredLevel(owner.getPlayerClass());
		if (requiredLevel == -1 || requiredLevel > owner.getLevel()) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getL10n(), requiredLevel));
			return null;
		}

		byte levelRestrict = itemTemplate.getMaxLevelRestrict(owner.getPlayerClass());
		if (levelRestrict != 0 && owner.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, itemTemplate.getL10n()));
			return null;
		}

		if (itemTemplate.getRace() != Race.PC_ALL && itemTemplate.getRace() != owner.getRace()) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_RACE());
			return null;
		}

		ItemUseLimits limits = itemTemplate.getUseLimits();
		if (limits.getGenderPermitted() != null && limits.getGenderPermitted() != owner.getGender()) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_GENDER());
			return null;
		}

		if (!verifyRankLimits(item)) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_RANK(AbyssRankEnum.getRankL10n(owner.getRace(), limits.getMinRank())));
			return null;
		}

		if (!checkInventorySlots(slot)) {
			PacketSendUtility.sendPacket(owner, STR_UI_INVENTORY_FULL());
			return null;
		}

		if (!checkAvailableEquipSkills(item))
			return null;

		if (!checkDualWieldRestriction(item, slot))
			return null;

		ItemSlot[] targetSlots = ItemSlot.getSlotsFor(slot);
		if (targetSlots.length == 0) {
			log.warn("Unknown target slot " + slot + " for " + item);
			return null;
		}

		if (targetSlots.length == 2 && !itemTemplate.isTwoHandWeapon() || targetSlots.length > 2) {
			AuditLogger.log(owner, "tried to equip " + item + " in slots: " + Arrays.toString(targetSlots));
			return null;
		}

		if ((ItemSlot.MAIN_OFF_OR_SUB_OFF.getSlotIdMask() & slot) != 0) { // offhand slots cannot be directly populated on client side
			AuditLogger.log(owner, "tried to equip " + item + " directly in offhand slot");
			return null;
		}

		long validSlotMask = itemTemplate.getItemSlot();
		if (validSlotMask == 0) // e.g. arrows, which cannot be equipped anymore
			return null;
		if ((validSlotMask & slot) != slot) { // invalid slot provided for the item
			AuditLogger.log(owner, "tried to equip " + item + " in invalid slot(s): " + Arrays.toString(targetSlots));
			return null;
		}

		if (!StigmaService.notifyEquipAction(owner, item, slot))
			return null;

		if (itemTemplate.isSoulBound() && !item.isSoulBound()) {
			soulBindItem(owner, item, slot);
			return null;
		}
		return equip(slot, item);
	}

	private boolean checkInventorySlots(long itemSlotToEquip) {
		if (owner.getInventory().isFull() && ItemSlot.isTwoHandedWeapon(itemSlotToEquip)) { // weapon slot(s)
			for (ItemSlot slot : ItemSlot.getSlotsFor(itemSlotToEquip)) {
				Item equippedWeaponOrShield = equipment.get(slot.getSlotIdMask());
				if (equippedWeaponOrShield == null || equippedWeaponOrShield.getItemTemplate().isTwoHandWeapon())
					return true;
			}
			return false; // two weapons would need to be unequipped, but there is no free slot
		}
		return true;
	}

	private boolean checkDualWieldRestriction(Item item, long slot) {
		if (item.getEquipmentType() == EquipType.WEAPON && !item.getItemTemplate().isTwoHandWeapon()) {
			if ((slot & ItemSlot.LEFT_HAND.getSlotIdMask()) == slot && !hasDualWieldingSkills())
				return false;
		}
		return true;
	}

	private Item equip(long itemSlotToEquip, Item item) {
		if (!item.isIdentified()) {
			log.warn(item + " can't be equipped because it's not identified yet");
			return null;
		}

		ItemSlot[] targetSlots = ItemSlot.getSlotsFor(itemSlotToEquip);

		synchronized (this) {
			// do unequip of necessary items
			unEquip(getUnequipSlots(itemSlotToEquip));
			owner.getInventory().remove(item);
			// equip target item
			for (ItemSlot slot : targetSlots)
				equipment.put(slot.getSlotIdMask(), item);
			item.setEquipped(true);
			item.setEquipmentSlot(itemSlotToEquip);
			ItemPacketService.updateItemAfterEquip(owner, item);

			// update stats
			notifyItemEquipped(item);
			owner.getLifeStats().updateCurrentStats();
			owner.getGameStats().updateStatsAndSpeedVisually();
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			QuestEngine.getInstance().onEquipItem(new QuestEnv(null, owner, 0), item.getItemId());

			if (item.getItemTemplate().isStigma())
				StigmaService.addLinkedStigmaSkills(owner);

			return item;
		}
	}

	private long getUnequipSlots(long itemSlotToEquip) {
		if (itemSlotToEquip == ItemSlot.MAIN_HAND.getSlotIdMask() || itemSlotToEquip == ItemSlot.SUB_HAND.getSlotIdMask()) {
			Item equippedItem = equipment.get(itemSlotToEquip);
			if (equippedItem != null && equippedItem.getItemTemplate().isTwoHandWeapon())
				return ItemSlot.MAIN_OR_SUB.getSlotIdMask(); // two-handed occupies two slots, so we need to unequip both
		}
		return itemSlotToEquip;
	}

	private void notifyItemEquipped(Item item) {
		ItemEquipmentListener.onItemEquipment(item, owner);
		owner.getObserveController().notifyItemEquip(item, owner);
		tryUpdateSummonStats();
	}

	private void notifyItemUnequip(Item item) {
		ItemEquipmentListener.onItemUnequipment(item, owner);
		owner.getObserveController().notifyItemUnEquip(item, owner);
		tryUpdateSummonStats();
	}

	private void tryUpdateSummonStats() {
		Summon summon = owner.getSummon();
		if (summon != null) {
			summon.getGameStats().updateStatsAndSpeedVisually();
		}
	}

	/**
	 * Called when CM_EQUIP_ITEM packet arrives with action 1
	 * 
	 * @return item or null in case of failure
	 */
	public Item unEquipItem(int itemObjId, boolean checkFullInventory) {
		// if inventory is full unequip action is disabled
		if (checkFullInventory && owner.getInventory().isFull())
			return null;

		synchronized (this) {
			Item itemToUnequip = getEquippedItemByObjId(itemObjId);
			if (itemToUnequip == null || !itemToUnequip.isEquipped())
				return null;

			// Looks very odd - but its retail like
			if (itemToUnequip.getEquipmentSlot() == ItemSlot.MAIN_HAND.getSlotIdMask()) {
				Item ohWeapon = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
				if (ohWeapon != null && ohWeapon.getItemTemplate().isWeapon()) {
					if (owner.getInventory().getFreeSlots() < 2) {
						return null;
					}
					unEquip(ItemSlot.SUB_HAND.getSlotIdMask());
				}
			}

			// if unequip power shard
			if (itemToUnequip.getItemTemplate().getItemGroup() == ItemGroup.POWER_SHARDS) {
				owner.unsetState(CreatureState.POWERSHARD);
				PacketSendUtility.sendPacket(owner, new SM_EMOTION(owner, EmotionType.POWERSHARD_OFF, 0, 0));
			}

			if (itemToUnequip.getItemTemplate().isStigma())
				StigmaService.removeStigmaSkills(owner, itemToUnequip.getItemTemplate().getStigma(), itemToUnequip.getEnchantLevel(), true);

			unEquip(itemToUnequip.getEquipmentSlot());

			return itemToUnequip;
		}
	}

	public Item unEquipItem(int itemObjId) {
		return unEquipItem(itemObjId, true);
	}

	/**
	 * @param slot
	 *          - Must be composite for dual weapons
	 */
	private void unEquip(long slot) {
		ItemSlot[] allSlots = ItemSlot.getSlotsFor(slot);
		for (ItemSlot itemSlot : allSlots) {
			Item item = equipment.remove(itemSlot.getSlotIdMask());
			if (item == null || !item.isEquipped()) // check isEquipped to avoid duplicate notifyUnequip, since two handed weapons occupy two slots
				continue;
			item.setEquipped(false);
			item.setEquipmentSlot(0);
			owner.getInventory().put(item);
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			notifyItemUnequip(item);
		}
		owner.getLifeStats().updateCurrentStats();
		owner.getGameStats().updateStatsAndSpeedVisually();
	}

	/**
	 * TODO: Move to SkillEngine Use skill stack SKILL_P_EQUIP_DUAL to check that instead
	 * 
	 * @return true if player can equip two one-handed weapons
	 */
	private boolean hasDualWieldingSkills() {
		return owner.getSkillList().isSkillPresent(55) || owner.getSkillList().isSkillPresent(171) || owner.getSkillList().isSkillPresent(143)
			|| owner.getSkillList().isSkillPresent(144) || owner.getSkillList().isSkillPresent(207);
	}

	private boolean checkAvailableEquipSkills(Item item) {
		int[] requiredSkills = item.getItemTemplate().getRequiredSkills();
		if (requiredSkills.length == 0) // if no skills required - validate as true
			return true;

		for (int skill : requiredSkills) {
			if (owner.getSkillList().isSkillPresent(skill))
				return true;
		}

		return false; // FIXME leather skill allows you to wear leather. You don't need cloth skill too!
	}

	public Item getEquippedItemByObjId(int itemObjId) {
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (item.getObjectId() == itemObjId)
					return item;
			}
		}
		return null;
	}

	public List<Item> getEquippedItemsByItemId(int value) {
		List<Item> equippedItemsById = new ArrayList<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (item.getItemTemplate().getTemplateId() == value)
					equippedItemsById.add(item);
			}
		}
		return equippedItemsById;
	}

	public List<Item> getEquippedItems() {
		synchronized (equipment) {
			return equipment.values().stream().distinct().collect(Collectors.toList());
		}
	}

	public Set<Integer> getEquippedItemIds() {
		synchronized (equipment) {
			return equipment.values().stream().map(Item::getItemId).collect(Collectors.toSet());
		}
	}

	public List<Item> getEquippedItemsWithoutStigma() {
		List<Item> equippedItems = new ArrayList<>();
		Set<Item> twoHanded = new HashSet<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (!ItemSlot.isStigma(item.getEquipmentSlot())) {
					if (item.getItemTemplate().isTwoHandWeapon() && !twoHanded.add(item))
						continue;
					equippedItems.add(item);
				}
			}
		}
		return equippedItems;
	}

	public List<Item> getEquippedForAppearance() {
		List<Item> equippedItems = new ArrayList<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (ItemSlot.isVisible(item.getEquipmentSlot()) && !(item.getItemTemplate().isTwoHandWeapon() && equippedItems.contains(item)))
					equippedItems.add(item);
			}
		}
		return equippedItems;
	}

	public List<Item> getEquippedItemsAllStigma() {
		List<Item> equippedItems = new ArrayList<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (ItemSlot.isStigma(item.getEquipmentSlot())) {
					equippedItems.add(item);
				}
			}
		}
		return equippedItems;
	}

	public List<Item> getEquippedItemsRegularStigma() {
		List<Item> equippedItems = new ArrayList<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (ItemSlot.isRegularStigma(item.getEquipmentSlot()))
					equippedItems.add(item);
			}
		}
		return equippedItems;
	}

	public List<Item> getEquippedItemsAdvancedStigma() {
		List<Item> equippedItems = new ArrayList<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (ItemSlot.isAdvancedStigma(item.getEquipmentSlot())) {
					equippedItems.add(item);
				}
			}
		}
		return equippedItems;
	}

	/**
	 * @return Number of parts equipped belonging to requested itemset
	 */
	public int itemSetPartsEquipped(int itemSetTemplateId) {
		int number = 0;
		List<Integer> counted = new ArrayList<>(); // no double counting for accessory and weapons

		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_OFF_HAND.getSlotIdMask()) != 0
						|| (item.getEquipmentSlot() & ItemSlot.SUB_OFF_HAND.getSlotIdMask()) != 0) {
					continue;
				}
				ItemSetTemplate setTemplate = item.getItemTemplate().getItemSet();
				if (setTemplate != null && setTemplate.getId() == itemSetTemplateId && !counted.contains(item.getItemId())) {
					counted.add(item.getItemId());
					++number;
				}
			}
		}
		return number;
	}

	/**
	 * Should be called only when loading from DB for items isEquipped=1
	 */
	public void onLoadHandler(Item item) {
		if (!checkAvailableEquipSkills(item)) {
			putItemBackToInventory(item);
			return;
		}
		if (!checkDualWieldRestriction(item, item.getEquipmentSlot())) {
			putItemBackToInventory(item);
			return;
		}
		for (ItemSlot slot : ItemSlot.getSlotsFor(item.getEquipmentSlot())) { // two slots (main+sub) for two-handed weapons
			if (equipment.putIfAbsent(slot.getSlotIdMask(), item) != null) {
				log.warn("Duplicate equipped item in slot " + slot + " for " + owner);
				putItemBackToInventory(item);
			}
		}
	}

	private void putItemBackToInventory(Item item) {
		item.setEquipped(false);
		item.setEquipmentSlot(0);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		owner.getInventory().put(item);
	}

	/**
	 * Should be called only when equipment object totally constructed on player loading. Applies every equipped item stats modificators
	 */
	public void onLoadApplyEquipmentStats() {
		Item twoHanded = null;
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_OFF_HAND.getSlotIdMask()) == 0
						&& (item.getEquipmentSlot() & ItemSlot.SUB_OFF_HAND.getSlotIdMask()) == 0) {
					if (item.getItemTemplate().isTwoHandWeapon()) {
						if (twoHanded != null)
							continue;
						twoHanded = item;
					}
					ItemEquipmentListener.onItemEquipment(item, owner);
				}
			}
		}
		owner.getLifeStats().synchronizeWithMaxStats();
	}

	/**
	 * @return true or false
	 */
	public boolean isShieldEquipped() {
		Item subHandItem = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
		if (subHandItem == null)
			return false;
		ItemSubType shieldType = subHandItem.getItemTemplate().getItemSubType();
		return shieldType == ItemSubType.SHIELD;
	}

	public Item getEquippedShield() {
		Item subHandItem = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
		if (subHandItem == null)
			return null;
		ItemSubType shieldType = subHandItem.getItemTemplate().getItemSubType();
		return (shieldType == ItemSubType.SHIELD) ? subHandItem : null;
	}

	/**
	 * @return <tt>WeaponType</tt> of current weapon in main hand or null
	 */
	public ItemGroup getMainHandWeaponType() {
		Item mainHandItem = equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask());
		if (mainHandItem == null)
			return null;

		return mainHandItem.getItemTemplate().getItemGroup();
	}

	/**
	 * @return <tt>WeaponType</tt> of current weapon in off hand or null
	 */
	public ItemGroup getOffHandWeaponType() {
		Item offHandItem = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
		Item mainHandItem = equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask());
		if (mainHandItem == offHandItem)
			offHandItem = null;
		if (offHandItem != null && offHandItem.getItemTemplate().isWeapon())
			return offHandItem.getItemTemplate().getItemGroup();

		return null;
	}

	public boolean isPowerShardEquipped() {
		return getMainHandPowerShard() != null || getOffHandPowerShard() != null;
	}

	public Item getMainHandPowerShard() {
		return equipment.get(ItemSlot.POWER_SHARD_RIGHT.getSlotIdMask());
	}

	public Item getOffHandPowerShard() {
		return equipment.get(ItemSlot.POWER_SHARD_LEFT.getSlotIdMask());
	}

	public void usePowerShard(Item powerShardItem, int count) {
		decreaseEquippedItemCount(powerShardItem.getObjectId(), count);

		if (powerShardItem.getItemCount() <= 0) {// Search for next same power shards stack
			List<Item> powerShardStacks = owner.getInventory().getItemsByItemId(powerShardItem.getItemTemplate().getTemplateId());
			if (powerShardStacks.size() != 0) {
				equipItem(powerShardStacks.get(0).getObjectId(), powerShardItem.getEquipmentSlot());
			} else {
				PacketSendUtility.sendPacket(owner, STR_MSG_WEAPON_BOOST_MODE_BURN_OUT());
				owner.unsetState(CreatureState.POWERSHARD);
			}
		}
	}

	/**
	 * increase item count and return left count
	 */
	public long increaseEquippedItemCount(Item item, long count) {
		// Only Shards can be increased
		if (item.getItemTemplate().getItemGroup() != ItemGroup.POWER_SHARDS)
			return count;

		long leftCount = item.increaseItemCount(count);
		ItemPacketService.updateItemAfterInfoChange(owner, item, ItemUpdateType.STATS_CHANGE);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return leftCount;
	}

	public void decreaseEquippedItemCount(int itemObjId, int count) {
		Item equippedItem = getEquippedItemByObjId(itemObjId);

		if (equippedItem.getItemCount() >= count)
			equippedItem.decreaseItemCount(count);
		else
			equippedItem.decreaseItemCount(equippedItem.getItemCount());

		if (equippedItem.getItemCount() == 0) {
			equipment.remove(equippedItem.getEquipmentSlot());
			PacketSendUtility.sendPacket(owner, new SM_DELETE_ITEM(equippedItem.getObjectId()));
			InventoryDAO.store(equippedItem, owner);
		}

		ItemPacketService.updateItemAfterInfoChange(owner, equippedItem, ItemUpdateType.STATS_CHANGE);
		PacketSendUtility.broadcastPacket(owner, new SM_UPDATE_PLAYER_APPEARANCE(owner.getObjectId(), owner.getEquipment().getEquippedForAppearance()),
			true);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * Switch OFF and MAIN hands
	 */
	public void switchHands() {
		Item mainHandItem = equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask());
		Item subHandItem = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
		Item mainOffHandItem = equipment.get(ItemSlot.MAIN_OFF_HAND.getSlotIdMask());
		Item subOffHandItem = equipment.get(ItemSlot.SUB_OFF_HAND.getSlotIdMask());

		List<Item> equippedWeapon = new ArrayList<>();

		if (mainHandItem != null)
			equippedWeapon.add(mainHandItem);
		if (subHandItem != null && subHandItem != mainHandItem)
			equippedWeapon.add(subHandItem);
		if (mainOffHandItem != null)
			equippedWeapon.add(mainOffHandItem);
		if (subOffHandItem != null && subOffHandItem != mainOffHandItem)
			equippedWeapon.add(subOffHandItem);

		for (Item item : equippedWeapon) {
			if (item.getItemTemplate().isTwoHandWeapon()) {
				ItemSlot[] slots = ItemSlot.getSlotsFor(item.getEquipmentSlot());
				for (ItemSlot slot : slots)
					equipment.remove(slot.getSlotIdMask());
			} else {
				equipment.remove(item.getEquipmentSlot());
			}
			item.setEquipped(false);
			PacketSendUtility.sendPacket(owner, new SM_INVENTORY_UPDATE_ITEM(owner, item, ItemUpdateType.EQUIP_UNEQUIP));
			if (owner.getGameStats() != null) {
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0
					|| (item.getEquipmentSlot() & ItemSlot.SUB_HAND.getSlotIdMask()) != 0) {
					notifyItemUnequip(item);
				}
			}
		}

		for (Item item : equippedWeapon) {
			long oldSlots = item.getEquipmentSlot();
			if ((oldSlots & ItemSlot.RIGHT_HAND.getSlotIdMask()) != 0)
				oldSlots ^= ItemSlot.RIGHT_HAND.getSlotIdMask();
			if ((oldSlots & ItemSlot.LEFT_HAND.getSlotIdMask()) != 0)
				oldSlots ^= ItemSlot.LEFT_HAND.getSlotIdMask();
			item.setEquipmentSlot(oldSlots);
		}

		for (Item item : equippedWeapon) {
			if (item.getItemTemplate().isTwoHandWeapon()) {
				ItemSlot[] slots = ItemSlot.getSlotsFor(item.getEquipmentSlot());
				for (ItemSlot slot : slots)
					equipment.put(slot.getSlotIdMask(), item);
			} else {
				equipment.put(item.getEquipmentSlot(), item);
			}
			item.setEquipped(true);
			ItemPacketService.updateItemAfterEquip(owner, item);
		}

		if (owner.getGameStats() != null) {
			for (Item item : equippedWeapon) {
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0
					|| (item.getEquipmentSlot() & ItemSlot.SUB_HAND.getSlotIdMask()) != 0) {
					notifyItemEquipped(item);
				}
			}
		}

		owner.getLifeStats().updateCurrentStats();
		owner.getGameStats().updateStatsAndSpeedVisually();
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public boolean isWeaponEquipped(ItemSubType subType) {
		Item weapon = getMainHandWeapon();
		if (weapon != null && weapon.getItemTemplate().getItemSubType() == subType)
			return true;
		weapon = getOffHandWeapon();
		if (weapon != null && weapon.getItemTemplate().getItemSubType() == subType)
			return true;
		return false;
	}

	/**
	 * Checks if dual one-handed weapon is equiped in any slot combination
	 */
	public boolean hasDualWeaponEquipped(ItemSlot slot) {
		ItemSlot[] slotValues = ItemSlot.getSlotsFor(slot.getSlotIdMask());
		if (slotValues.length == 0)
			return false;
		for (ItemSlot s : slotValues) {
			Item weapon = equipment.get(s.getSlotIdMask());
			if (weapon == null || weapon.getItemTemplate().isTwoHandWeapon())
				continue;
			if (weapon.getItemTemplate().isWeapon())
				return true;
		}
		return false;
	}

	public boolean isArmorEquipped(ItemSubType subType) {
		for (long slot : ARMOR_SLOTS) {
			Item item = equipment.get(slot);
			if (item != null && item.getItemTemplate().getItemSubType() == subType)
				return true;
		}
		return false;
	}

	/**
	 * Only used for new Player creation. Although invalid, but fits its purpose
	 */
	public boolean isSlotEquipped(long slot) {
		return equipment.get(slot) != null;
	}

	public Item getMainHandWeapon() {
		return equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask());
	}

	public Item getOffHandWeapon() {
		Item result = equipment.get(ItemSlot.SUB_HAND.getSlotIdMask());
		if (getMainHandWeapon() == result)
			return null;
		return result;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	private boolean soulBindItem(final Player player, final Item item, final long slot) {
		if (player.getInventory().getItemByObjId(item.getObjectId()) == null)
			return false;
		if (player.isDead()) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400059)));
			return false;
		} else if (player.isInPlayerMode(PlayerMode.RIDE)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400056)));
			return false;
		} else if (player.isInState(CreatureState.CHAIR)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400058)));
			return false;
		} else if (player.isInState(CreatureState.RESTING)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400057)));
			return false;
		} else if (player.isInState(CreatureState.GLIDING)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400082)));
			return false;
		} else if (player.isInState(CreatureState.FLYING)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400055)));
			return false;
		} else if (player.isInState(CreatureState.WEAPON_EQUIPPED)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(ChatUtil.l10n(1400079)));
			return false;
		}

		RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(player) {

			@Override
			public void acceptRequest(Player requester, Player responder) {
				responder.getController().cancelUseItem();

				PacketSendUtility.broadcastPacket(responder,
					new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 5000, 4), true);

				responder.getController().cancelTask(TaskId.ITEM_USE);

				final ActionObserver moveObserver = new ActionObserver(ObserverType.MOVE) {

					@Override
					public void moved() {
						responder.getController().cancelTask(TaskId.ITEM_USE);
						PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_CANCELED(item.getL10n()));
						PacketSendUtility.broadcastPacket(responder,
							new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 0, 8), true);
					}
				};
				responder.getObserveController().attach(moveObserver);

				// item usage animation
				responder.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						responder.getObserveController().removeObserver(moveObserver);

						PacketSendUtility.broadcastPacket(responder,
							new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 0, 6), true);
						PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_SUCCEED(item.getL10n()));

						item.setSoulBound(true);
						ItemPacketService.updateItemAfterInfoChange(owner, item);

						equip(slot, item);
						PacketSendUtility.broadcastPacket(responder, new SM_UPDATE_PLAYER_APPEARANCE(responder.getObjectId(), getEquippedForAppearance()), true);
					}
				}, 5000));
			}

			@Override
			public void denyRequest(Player requester, Player responder) {
				PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_CANCELED(item.getL10n()));
			}
		};

		boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, responseHandler);
		if (requested) {
			PacketSendUtility.sendPacket(player,
				new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, 0, 0, item.getL10n()));
		} else {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_CLOSE_OTHER_MSG_BOX_AND_RETRY());
		}
		return false;
	}

	private boolean verifyRankLimits(Item item) {
		int rank = owner.getAbyssRank().getRank().getId();
		if (!item.getItemTemplate().getUseLimits().verifyRank(rank))
			return false;
		if (item.getFusionedItemTemplate() != null)
			return item.getFusionedItemTemplate().getUseLimits().verifyRank(rank);
		return true;
	}

	public void checkRankLimitItems() {
		for (Item item : getEquippedItems()) {
			if (!verifyRankLimits(item)) {
				unEquipItem(item.getObjectId(), false);
				PacketSendUtility.sendPacket(owner, STR_MSG_UNEQUIP_RANKITEM(item.getL10n()));
				// TODO: Check retail what happens with full inv and the task msgs.
			}
		}
	}
}
