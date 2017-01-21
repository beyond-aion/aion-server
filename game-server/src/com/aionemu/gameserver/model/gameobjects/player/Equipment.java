package com.aionemu.gameserver.model.gameobjects.player;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_CLASS;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_GENDER;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RANK;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_UNEQUIP_RANKITEM;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_WEAPON_BOOST_MODE_BURN_OUT;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_CLOSE_OTHER_MSG_BOX_AND_RETRY;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_ITEM_CANCELED;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_ITEM_SUCCEED;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.StigmaService;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

import javolution.util.FastTable;

/**
 * @author Avol, ATracer, kosyachok
 * @modified cura
 */
public class Equipment {

	private static final Logger log = LoggerFactory.getLogger(Equipment.class);

	private SortedMap<Long, Item> equipment = new TreeMap<>();
	private Player owner;

	private Set<Long> markedFreeSlots = new HashSet<>();
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
	 * @param itemUniqueId
	 * @param slot
	 * @return item or null in case of failure
	 */
	public Item equipItem(int itemUniqueId, long slot) {
		Item item = owner.getInventory().getItemByObjId(itemUniqueId);

		if (item == null)
			return null;

		ItemTemplate itemTemplate = item.getItemTemplate();

		if (!item.getItemTemplate().isClassSpecific(owner.getPlayerClass())) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_CLASS());
			return null;
		}
		// don't allow to wear items of not allowed level
		int requiredLevel = item.getItemTemplate().getRequiredLevel(owner.getPlayerClass());
		if (requiredLevel == -1 || requiredLevel > owner.getLevel()) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getNameId(), requiredLevel));
			return null;
		}

		byte levelRestrict = itemTemplate.getMaxLevelRestrict(owner.getPlayerClass());
		if (levelRestrict != 0 && owner.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, itemTemplate.getNameId()));
			return null;
		}

		if (owner.getAccessLevel() < AdminConfig.GM_LEVEL) {
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
				PacketSendUtility.sendPacket(owner, STR_CANNOT_USE_ITEM_INVALID_RANK(AbyssRankEnum.getRankById(limits.getMinRank()).getDescriptionId()));
				return null;
			}
		}

		long itemSlotToEquip = 0;

		synchronized (equipment) {
			markedFreeSlots.clear();

			// validate item against current equipment and mark free slots
			long oldSlot = item.getEquipmentSlot();
			item.setEquipmentSlot(slot);
			switch (item.getEquipmentType()) {
				case ARMOR:
					if (!validateEquippedArmor(item, true)) {
						item.setEquipmentSlot(oldSlot);
						return null;
					}
					break;
				case WEAPON:
					if (!validateEquippedWeapon(item, true)) {
						item.setEquipmentSlot(oldSlot);
						return null;
					}
					break;
			}

			// check whether there is already item in specified slot
			long itemSlotMask = 0;
			switch (item.getEquipmentType()) {
				case STIGMA:
					itemSlotMask = slot;
					break;
				default:
					itemSlotMask = itemTemplate.getItemSlot();
					break;
			}

			ItemSlot[] possibleSlots = ItemSlot.getSlotsFor(itemSlotMask);
			// find correct slot
			for (ItemSlot possibleSlot : possibleSlots) {
				long slotId = possibleSlot.getSlotIdMask();
				if (equipment.get(slotId) == null || markedFreeSlots.contains(slotId)) {
					if (item.getItemTemplate().isTwoHandWeapon()) {
						itemSlotMask &= ~slotId;
					} else {
						itemSlotToEquip = slotId;
						break;
					}
				}
			}
			if (item.getItemTemplate().isTwoHandWeapon()) {
				if (itemSlotMask != 0)
					return null;
				itemSlotToEquip = itemTemplate.getItemSlot();
			}

			if (!StigmaService.notifyEquipAction(owner, item, slot))
				return null;

			// equip first occupied slot if there is no free
			if (itemSlotToEquip == 0) {
				itemSlotToEquip = possibleSlots[0].getSlotIdMask();
			}
		}

		if (itemSlotToEquip == 0)
			return null;

		if (itemTemplate.isSoulBound() && !item.isSoulBound()) {
			soulBindItem(owner, item, itemSlotToEquip);
			return null;
		}
		return equip(itemSlotToEquip, item);
	}

	/**
	 * @param itemSlotToEquip
	 *          - must be slot combination for dual weapons
	 * @param item
	 */
	private Item equip(long itemSlotToEquip, Item item) {
		if (!item.isIdentified()) {
			log.warn("Item {} (ID:{}) can't be equipped because it's not identified yet", item.getObjectId(), item.getItemId());
			return null;
		}
		synchronized (equipment) {
			ItemSlot[] allSlots = ItemSlot.getSlotsFor(itemSlotToEquip);
			if (allSlots.length > 1 && !item.getItemTemplate().isTwoHandWeapon())
				throw new IllegalArgumentException("itemSlotToEquip can not be composite!");

			// remove item first from inventory to have at least one slot free
			owner.getInventory().remove(item);

			// do unequip of necessary items
			Item equippedItem = equipment.get(allSlots[0].getSlotIdMask());
			if (equippedItem != null) {
				if (equippedItem.getItemTemplate().isTwoHandWeapon())
					unEquip(equippedItem.getEquipmentSlot());
				else {
					for (ItemSlot slot : allSlots)
						unEquip(slot.getSlotIdMask());
				}
			}

			switch (item.getEquipmentType()) {
				case ARMOR:
					validateEquippedArmor(item, false);
					break;
				case WEAPON:
					validateEquippedWeapon(item, false);
					break;
			}

			if (equipment.get(allSlots[0].getSlotIdMask()) != null) {
				log.error("CHECKPOINT : putting item to already equiped slot. Info slot: " + itemSlotToEquip + " new item: "
					+ item.getItemTemplate().getTemplateId() + " old item: " + equipment.get(allSlots[0].getSlotIdMask()).getItemTemplate().getTemplateId());
				return null;
			}

			// equip target item
			for (ItemSlot slot : allSlots)
				equipment.put(slot.getSlotIdMask(), item);
			item.setEquipped(true);
			item.setEquipmentSlot(itemSlotToEquip);
			ItemPacketService.updateItemAfterEquip(owner, item);

			// update stats
			notifyItemEquipped(item);
			owner.getLifeStats().updateCurrentStats();
			owner.getGameStats().updateStatsAndSpeedVisually();
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			QuestEngine.getInstance().onEquipItem(new QuestEnv(null, owner, 0, 0), item.getItemId());

			if (item.getItemTemplate().isStigma())
				StigmaService.addLinkedStigmaSkills(owner);

			return item;
		}
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
	 * @param itemObjId
	 * @param slot
	 * @return item or null in case of failure
	 */
	public Item unEquipItem(int itemObjId, boolean checkFullInventory) {
		// if inventory is full unequip action is disabled
		if (checkFullInventory && owner.getInventory().isFull())
			return null;

		synchronized (equipment) {
			Item itemToUnequip = null;

			for (Item item : equipment.values()) {
				if (item.getObjectId() == itemObjId) {
					itemToUnequip = item;
					break;
				}
			}

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
		Item item = equipment.remove(allSlots[0].getSlotIdMask());
		if (item == null) { // NPE check, there is no item in the given slot.
			return;
		}
		if (allSlots.length > 1) {
			if (!item.getItemTemplate().isTwoHandWeapon()) {
				equipment.put(allSlots[0].getSlotIdMask(), item);
				throw new IllegalArgumentException("slot can not be composite!");
			}
			equipment.remove(allSlots[1].getSlotIdMask());
		}

		item.setEquipped(false);
		item.setEquipmentSlot(0);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		notifyItemUnequip(item);
		owner.getLifeStats().updateCurrentStats();
		owner.getGameStats().updateStatsAndSpeedVisually();
		owner.getInventory().put(item);
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

	/**
	 * Used during equip process and analyzes equipped slots
	 * 
	 * @param item
	 * @param itemInMainHand
	 * @param itemInSubHand
	 * @return
	 */
	private boolean validateEquippedWeapon(Item item, boolean validateOnly) {
		// Disable arrow equipment
		if (item.getItemTemplate().getItemGroup() == ItemGroup.ARROW)
			return false;

		// check present skill
		int[] requiredSkills = item.getItemTemplate().getRequiredSkills();

		if (!checkAvailableEquipSkills(requiredSkills))
			return false;

		Item itemInRightHand, itemInLeftHand;
		long rightSlot, leftSlot;
		if ((item.getEquipmentSlot() & ItemSlot.MAIN_OR_SUB.getSlotIdMask()) != 0) {
			rightSlot = ItemSlot.MAIN_HAND.getSlotIdMask();
			leftSlot = ItemSlot.SUB_HAND.getSlotIdMask();
			itemInRightHand = equipment.get(rightSlot);
			itemInLeftHand = equipment.get(leftSlot);
		} else if ((item.getEquipmentSlot() & ItemSlot.MAIN_OFF_OR_SUB_OFF.getSlotIdMask()) != 0) {
			rightSlot = ItemSlot.MAIN_OFF_HAND.getSlotIdMask();
			leftSlot = ItemSlot.SUB_OFF_HAND.getSlotIdMask();
			itemInRightHand = equipment.get(rightSlot);
			itemInLeftHand = equipment.get(leftSlot);
		} else
			return false;

		// for dual weapons, they occupy two slots, so check if the same item
		if (itemInRightHand == itemInLeftHand)
			itemInLeftHand = null;

		int requiredInventorySlots = 0;
		boolean mainIsTwoHand = itemInRightHand != null && itemInRightHand.getItemTemplate().isTwoHandWeapon();

		if (item.getItemTemplate().isTwoHandWeapon()) {
			if (mainIsTwoHand) {
				if (validateOnly) {
					requiredInventorySlots++;
					markedFreeSlots.add(rightSlot);
					markedFreeSlots.add(leftSlot);
				} else
					unEquip(rightSlot | leftSlot);
			} else {
				if (itemInRightHand != null) {
					if (validateOnly) {
						requiredInventorySlots++;
						markedFreeSlots.add(rightSlot);
					} else
						unEquip(rightSlot);
				}
				if (itemInLeftHand != null) {
					if (validateOnly) {
						requiredInventorySlots++;
						markedFreeSlots.add(leftSlot);
					} else
						unEquip(leftSlot);
				}
			}
		} else { // adding one-handed weapon
			if (itemInRightHand != null) { // main hand is already occupied
				boolean addingLeftHand = (item.getEquipmentSlot() & ItemSlot.LEFT_HAND.getSlotIdMask()) != 0;
				// if occupied by 2H weapon, we have to unequip both slots, skills are not required
				if (mainIsTwoHand) {
					if (validateOnly) {
						requiredInventorySlots++;
						markedFreeSlots.add(rightSlot);
						markedFreeSlots.add(leftSlot);
					} else
						unEquip(rightSlot | leftSlot);
				} // main hand is already occupied and adding unknown hand, needs skills to be checked
				else if (hasDualWieldingSkills()) {
					// if adding to empty left hand that is ok
					if (itemInLeftHand == null && addingLeftHand)
						return true;

					long switchSlot = addingLeftHand ? leftSlot : rightSlot;

					if (validateOnly) {
						requiredInventorySlots++;
						markedFreeSlots.add(switchSlot);
					} else
						unEquip(switchSlot);
				} else {
					// requiredInventorySlots are 0
					if (addingLeftHand && itemInLeftHand != null && !isShieldEquipped()) {
						// this is not good, if inventory is full, should switch slots
						if (validateOnly)
							markedFreeSlots.add(leftSlot);
						else
							unEquip(leftSlot);
					} else {
						// replace main hand, doesn't matter which slot is equiped
						// client sends slot 2 even for double-click
						if (validateOnly)
							markedFreeSlots.add(rightSlot);
						else
							unEquip(rightSlot);
						item.setEquipmentSlot(rightSlot);
						return true;
					}
				}
			}
		}

		// check again = required slots
		return requiredInventorySlots == 0 || owner.getInventory().getFreeSlots() >= requiredInventorySlots;
	}

	/**
	 * @param requiredSkills
	 * @return
	 */
	public boolean checkAvailableEquipSkills(int[] requiredSkills) {
		if (!owner.getCommonData().isOnline() && owner.getSkillList().size() <= 10) // exclusion for 4.8 skill update
			return true;

		if (requiredSkills.length == 0) // if no skills required - validate as true
			return true;

		for (int skill : requiredSkills) {
			if (owner.getSkillList().isSkillPresent(skill))
				return true;
		}

		return false;
	}

	/**
	 * Used during equip process and analyzes equipped slots
	 * 
	 * @param item
	 * @param itemInMainHand
	 * @return
	 */
	private boolean validateEquippedArmor(Item item, boolean validateOnly) {
		ItemTemplate template = item.getItemTemplate();
		if (template.getItemGroup() == ItemGroup.ARROW)
			return false;
		// allow wearing of jewelry etc stuff
		if (!template.isArmor())
			return true;

		// check present skill
		int[] requiredSkills = item.getItemTemplate().getRequiredSkills();
		if (!checkAvailableEquipSkills(requiredSkills))
			return false;

		ItemSlot slotToCheck1 = ItemSlot.MAIN_HAND;
		ItemSlot slotToCheck2 = ItemSlot.SUB_HAND;
		if ((item.getEquipmentSlot() & ItemSlot.MAIN_OFF_OR_SUB_OFF.getSlotIdMask()) != 0) {
			slotToCheck1 = ItemSlot.MAIN_OFF_HAND;
			slotToCheck2 = ItemSlot.SUB_OFF_HAND;
		}

		Item itemInMainHand = equipment.get(slotToCheck1.getSlotIdMask());
		if (itemInMainHand != null && template.getItemSubType() == ItemSubType.SHIELD && itemInMainHand.getItemTemplate().isTwoHandWeapon()) {
			if (validateOnly) {
				if (owner.getInventory().isFull())
					return false;
				markedFreeSlots.add(slotToCheck1.getSlotIdMask());
				markedFreeSlots.add(slotToCheck2.getSlotIdMask());
			} else {
				// remove 2H weapon
				unEquip(slotToCheck1.getSlotIdMask() | slotToCheck2.getSlotIdMask());
			}
		}
		return true;
	}

	/**
	 * Will look item in equipment item set
	 * 
	 * @param value
	 * @return Item
	 */
	public Item getEquippedItemByObjId(int value) {
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (item.getObjectId() == value)
					return item;
			}
		}

		return null;
	}

	/**
	 * @param value
	 * @return List<Item>
	 */
	public List<Item> getEquippedItemsByItemId(int value) {
		List<Item> equippedItemsById = new FastTable<>();
		synchronized (equipment) {
			for (Item item : equipment.values()) {
				if (item.getItemTemplate().getTemplateId() == value)
					equippedItemsById.add(item);
			}
		}

		return equippedItemsById;
	}

	/**
	 * @return List<Item>
	 */
	public List<Item> getEquippedItems() {
		HashSet<Item> equippedItems = new HashSet<>();
		equippedItems.addAll(equipment.values());

		return Arrays.asList(equippedItems.toArray(new Item[0]));
	}

	public List<Integer> getEquippedItemIds() {
		HashSet<Integer> equippedIds = new HashSet<>();
		for (Item i : equipment.values()) {
			equippedIds.add(i.getItemId());
		}
		return Arrays.asList(equippedIds.toArray(new Integer[0]));
	}

	/**
	 * @return List<Item>
	 */
	public FastTable<Item> getEquippedItemsWithoutStigma() {
		FastTable<Item> equippedItems = new FastTable<>();
		List<Item> twoHanded = new FastTable<>();
		for (Item item : equipment.values()) {
			if (!ItemSlot.isStigma(item.getEquipmentSlot())) {
				if (item.getItemTemplate().isTwoHandWeapon()) {
					if (twoHanded.contains(item))
						continue;
					twoHanded.add(item);
				}
				equippedItems.add(item);
			}
		}
		twoHanded.clear();
		twoHanded = null;
		return equippedItems;
	}

	public List<Item> getEquippedForAppearence() {
		List<Item> equippedItems = new FastTable<>();
		for (Item item : equipment.values()) {
			if (ItemSlot.isVisible(item.getEquipmentSlot()) && !(item.getItemTemplate().isTwoHandWeapon() && equippedItems.contains(item)))
				equippedItems.add(item);
		}

		return equippedItems;
	}

	/**
	 * @return List<Item>
	 */
	public List<Item> getEquippedItemsAllStigma() {
		List<Item> equippedItems = new FastTable<>();
		for (Item item : equipment.values()) {
			if (ItemSlot.isStigma(item.getEquipmentSlot())) {
				equippedItems.add(item);
			}
		}
		return equippedItems;
	}

	/**
	 * @return List<Item>
	 */
	public List<Item> getEquippedItemsRegularStigma() {
		List<Item> equippedItems = new FastTable<>();
		for (Item item : equipment.values()) {
			if (ItemSlot.isRegularStigma(item.getEquipmentSlot()))
				equippedItems.add(item);
		}
		return equippedItems;
	}

	/**
	 * @return List<Item>
	 */
	public List<Item> getEquippedItemsAdvancedStigma() {
		List<Item> equippedItems = new FastTable<>();
		for (Item item : equipment.values()) {
			if (ItemSlot.isAdvancedStigma(item.getEquipmentSlot())) {
				equippedItems.add(item);
			}
		}
		return equippedItems;
	}

	/**
	 * @return Number of parts equipped belonging to requested itemset
	 */
	public int itemSetPartsEquipped(int itemSetTemplateId) {
		int number = 0;
		List<Integer> counted = new FastTable<>(); // no double counting for accessory and weapons

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
		counted.clear();
		return number;
	}

	/**
	 * Should be called only when loading from DB for items isEquipped=1
	 * 
	 * @param item
	 */
	public void onLoadHandler(Item item) {
		ItemTemplate template = item.getItemTemplate();
		// unequip arrows during upgrade to 4.0, and put back to inventory
		// do some check for item level as well
		if (template.isArmor()) {
			if (!validateEquippedArmor(item, true)) {
				putItemBackToInventory(item);
				return;
			}
		}
		if (template.isWeapon()) {
			if (!validateEquippedWeapon(item, true)) {
				putItemBackToInventory(item);
				return;
			}
		}
		if (template.isTwoHandWeapon()) {
			ItemSlot[] oldSlots = ItemSlot.getSlotsFor(item.getEquipmentSlot());
			if (oldSlots.length != 2) {
				// update slot during upgrade to 4.0
				long currentSlot = item.getEquipmentSlot();
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_OR_SUB.getSlotIdMask()) != 0)
					item.setEquipmentSlot(ItemSlot.MAIN_OR_SUB.getSlotIdMask());
				else
					item.setEquipmentSlot(ItemSlot.MAIN_OFF_OR_SUB_OFF.getSlotIdMask());
				if (currentSlot != item.getEquipmentSlot())
					setPersistentState(PersistentState.UPDATE_REQUIRED);
				oldSlots = ItemSlot.getSlotsFor(item.getEquipmentSlot());
			}
			for (ItemSlot sl : oldSlots) {
				if (equipment.containsKey(sl.getSlotIdMask())) {
					log.warn("Duplicate equipped item in slot : " + sl.getSlotIdMask() + " " + owner.getObjectId());
					putItemBackToInventory(item);
					break;
				}
				equipment.put(sl.getSlotIdMask(), item);
			}
			return;
		}

		if (equipment.containsKey(item.getEquipmentSlot())) {
			log.warn("Duplicate equipped item in slot: " + item.getEquipmentSlot() + " " + owner.getObjectId());
			putItemBackToInventory(item);
			return;
		}
		equipment.put(item.getEquipmentSlot(), item);
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
	 * @return true if player is equipping the requested ArmorType
	 */
	public boolean isArmorTypeEquipped(ItemSubType type) {
		for (Item item : equipment.values()) {
			if (item == null || item.getItemTemplate().isWeapon())
				continue;
			// TODO: Check it! Not sure for dual hand
			if (item.getItemTemplate().isArmor()) {
				if (item.getItemTemplate().getItemSubType() == type && item.isEquipped() && item.getEquipmentSlot() != ItemSlot.SUB_OFF_HAND.getSlotIdMask()) {
					return true;
				}
			}
		}
		return false;
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
		Item leftPowershard = equipment.get(ItemSlot.POWER_SHARD_LEFT.getSlotIdMask());
		if (leftPowershard != null)
			return true;

		Item rightPowershard = equipment.get(ItemSlot.POWER_SHARD_RIGHT.getSlotIdMask());
		if (rightPowershard != null)
			return true;

		return false;
	}

	public Item getMainHandPowerShard() {
		Item mainHandPowerShard = equipment.get(ItemSlot.POWER_SHARD_RIGHT.getSlotIdMask());
		if (mainHandPowerShard != null)
			return mainHandPowerShard;

		return null;
	}

	public Item getOffHandPowerShard() {
		Item offHandPowerShard = equipment.get(ItemSlot.POWER_SHARD_LEFT.getSlotIdMask());
		if (offHandPowerShard != null)
			return offHandPowerShard;

		return null;
	}

	/**
	 * @param powerShardItem
	 * @param count
	 */
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
			DAOManager.getDAO(InventoryDAO.class).store(equippedItem, owner);
		}

		ItemPacketService.updateItemAfterInfoChange(owner, equippedItem, ItemUpdateType.STATS_CHANGE);
		PacketSendUtility.broadcastPacket(owner, new SM_UPDATE_PLAYER_APPEARANCE(owner.getObjectId(), owner.getEquipment().getEquippedForAppearence()),
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

		List<Item> equippedWeapon = new FastTable<>();

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
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0 || (item.getEquipmentSlot() & ItemSlot.SUB_HAND.getSlotIdMask()) != 0) {
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
				if ((item.getEquipmentSlot() & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0 || (item.getEquipmentSlot() & ItemSlot.SUB_HAND.getSlotIdMask()) != 0) {
					notifyItemEquipped(item);
				}
			}
		}

		owner.getLifeStats().updateCurrentStats();
		owner.getGameStats().updateStatsAndSpeedVisually();
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @param weaponType
	 */
	public boolean isWeaponEquipped(ItemSubType subType) {
		if (equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask()) != null
			&& equipment.get(ItemSlot.MAIN_HAND.getSlotIdMask()).getItemTemplate().getItemSubType() == subType) {
			return true;
		}
		if (equipment.get(ItemSlot.SUB_HAND.getSlotIdMask()) != null
			&& equipment.get(ItemSlot.SUB_HAND.getSlotIdMask()).getItemTemplate().getItemSubType() == subType) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if dual one-handed weapon is equiped in any slot combination
	 * 
	 * @param slot
	 *          masks
	 * @return
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

	/**
	 * @param armorType
	 */
	public boolean isArmorEquipped(ItemSubType subType) {

		for (long slot : ARMOR_SLOTS) {
			if (equipment.get(slot) != null && equipment.get(slot).getItemTemplate().getItemSubType() == subType)
				return true;
		}
		return false;
	}

	/**
	 * Only used for new Player creation. Although invalid, but fits its purpose
	 * 
	 * @param slot
	 * @return
	 */
	public boolean isSlotEquipped(long slot) {
		return !(equipment.get(slot) == null);
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

	/**
	 * @return the persistentState
	 */
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the persistentState to set
	 */
	public void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	/**
	 * @param player
	 */
	public void setOwner(Player player) {
		this.owner = player;
	}

	/**
	 * @param player
	 * @param item
	 * @return
	 */
	private boolean soulBindItem(final Player player, final Item item, final long slot) {
		if (player.getInventory().getItemByObjId(item.getObjectId()) == null || player.isInState(CreatureState.GLIDING))
			return false;
		if (PlayerActions.isAlreadyDead(player)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800119));
			return false;
		} else if (player.isInPlayerMode(PlayerMode.RIDE)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800114));
			return false;
		} else if (player.isInState(CreatureState.CHAIR)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800117));
			return false;
		} else if (player.isInState(CreatureState.RESTING)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800115));
			return false;
		} else if (player.isInState(CreatureState.FLYING)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800111));
			return false;
		} else if (player.isInState(CreatureState.WEAPON_EQUIPPED)) {
			PacketSendUtility.sendPacket(player, STR_SOUL_BOUND_INVALID_STANCE(2800159));
			return false;
		}

		RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(player) {

			@Override
			public void acceptRequest(Player requester, Player responder) {
				responder.getController().cancelUseItem();

				PacketSendUtility.broadcastPacket(responder,
					new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 5000, 4),
					true);

				responder.getController().cancelTask(TaskId.ITEM_USE);

				final ActionObserver moveObserver = new ActionObserver(ObserverType.MOVE) {

					@Override
					public void moved() {
						responder.getController().cancelTask(TaskId.ITEM_USE);
						PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_CANCELED(item.getNameId()));
						PacketSendUtility.broadcastPacket(responder,
							new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 0, 8),
							true);
					}
				};
				responder.getObserveController().attach(moveObserver);

				// item usage animation
				responder.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						responder.getObserveController().removeObserver(moveObserver);

						PacketSendUtility.broadcastPacket(responder,
							new SM_ITEM_USAGE_ANIMATION(responder.getObjectId(), item.getObjectId(), item.getItemId(), 0, 6),
							true);
						PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_SUCCEED(item.getNameId()));

						item.setSoulBound(true);
						ItemPacketService.updateItemAfterInfoChange(owner, item);

						equip(slot, item);
						PacketSendUtility.broadcastPacket(responder, new SM_UPDATE_PLAYER_APPEARANCE(responder.getObjectId(), getEquippedForAppearence()), true);
					}
				}, 5000));
			}

			@Override
			public void denyRequest(Player requester, Player responder) {
				PacketSendUtility.sendPacket(responder, STR_SOUL_BOUND_ITEM_CANCELED(item.getNameId()));
			}
		};

		boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, responseHandler);
		if (requested) {
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, 0, 0,
				new DescriptionId(item.getNameId())));
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
				PacketSendUtility.sendPacket(owner, STR_MSG_UNEQUIP_RANKITEM(item.getNameId()));
				// TODO: Check retail what happens with full inv and the task msgs.
			}
		}
	}
}
