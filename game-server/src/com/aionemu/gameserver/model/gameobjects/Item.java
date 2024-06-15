package com.aionemu.gameserver.model.gameobjects;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.enchants.TemperingEffect;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.*;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.ItemStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.Improvement;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Wakizashi, xTz
 */
public class Item extends AionObject implements Expirable, StatOwner, Persistable {

	public static final int MAX_BASIC_STONES = 6;
	private static final Logger log = LoggerFactory.getLogger(Item.class);
	private long itemCount = 1;
	private Integer itemColor;
	private int colorExpireTime = 0;
	private String itemCreator;
	private final ItemTemplate itemTemplate;
	private ItemTemplate itemSkinTemplate;
	private ItemTemplate fusionedItemTemplate;
	private boolean isEquipped = false;
	private long equipmentSlot = ItemStorage.FIRST_AVAILABLE_SLOT;
	private PersistentState persistentState;
	private Set<ManaStone> manaStones;
	private Set<ManaStone> fusionStones;
	private int optionalSockets;
	private int fusionedItemOptionalSockets;
	private GodStone godStone;
	private IdianStone idianStone;
	private boolean isSoulBound = false;
	private int itemLocation;
	private int enchantLevel;
	private int enchantBonus;
	private int expireTime = 0;
	private int temporaryExchangeTime = 0;
	private long repurchasePrice;
	private int activationCount = 0;
	private ChargeInfo conditioningInfo;
	private List<StatFunction> currentModifiers;
	private int tuneCount = 0;
	private RandomBonusEffect bonusStatsEffect;
	private RandomBonusEffect fusionedItemBonusStatsEffect;
	private int packCount;
	private int tempering;
	private EnchantEffect enchantEffect;
	private TemperingEffect temperingEffect;
	private boolean isAmplified = false;
	private int buffSkill;
	private int rndPlumeBonusValue;
	private PendingTuneResult pendingTuneResult;

	/**
	 * Create simple item with minimum information
	 */
	public Item(int objId, ItemTemplate itemTemplate) {
		super(objId);
		this.itemTemplate = itemTemplate;
		this.activationCount = itemTemplate.getActivationCount();
		if (itemTemplate.getExpireTime() != 0)
			expireTime = ((int) (System.currentTimeMillis() / 1000) + itemTemplate.getExpireTime() * 60) - 1;
		if (itemTemplate.canTune())
			tuneCount = -1; // not identified yet (bonus stats need to be rolled)
		isAmplified = itemTemplate.getEnchantType() == 1;
		this.persistentState = PersistentState.NEW;
		updateChargeInfo(0);
	}

	/**
	 * This constructor should be called from ItemService for newly created items and loadedFromDb
	 */
	public Item(int objId, ItemTemplate itemTemplate, long itemCount, boolean isEquipped, long equipmentSlot) {
		this(objId, itemTemplate);
		this.itemCount = itemCount;
		this.isEquipped = isEquipped;
		this.equipmentSlot = equipmentSlot;
	}

	/**
	 * This constructor should be called only from DAO while loading from DB
	 */
	public Item(int objId, int itemId, long itemCount, Integer itemColor, int colorExpires, String itemCreator, int expireTime, int activationCount,
		boolean isEquipped, boolean isSoulBound, long equipmentSlot, int itemLocation, int enchant, int enchantBonus, int itemSkin, int fusionedItem,
		int optionalSockets, int fusionedItemOptionalSockets, int charge, int tuneCount, int statBonusId, int fusionedItemStatBonusId, int tempering,
		int packCount, boolean isAmplified, int buffSkill, int rndPlumeBonusValue) {
		super(objId);

		this.itemTemplate = Objects.requireNonNull(DataManager.ITEM_DATA.getItemTemplate(itemId), () -> "Missing template for item " + itemId);
		this.itemCount = itemCount;
		this.itemColor = itemColor;
		this.colorExpireTime = colorExpires;
		this.itemCreator = itemCreator;
		this.expireTime = expireTime;
		this.activationCount = activationCount;
		this.isEquipped = isEquipped;
		this.isSoulBound = isSoulBound;
		this.equipmentSlot = equipmentSlot;
		this.itemLocation = itemLocation;
		this.enchantLevel = enchant;
		this.enchantBonus = enchantBonus;
		this.fusionedItemTemplate = DataManager.ITEM_DATA.getItemTemplate(fusionedItem);
		this.itemSkinTemplate = DataManager.ITEM_DATA.getItemTemplate(itemSkin);
		this.tuneCount = tuneCount;
		this.optionalSockets = optionalSockets;
		this.fusionedItemOptionalSockets = fusionedItemOptionalSockets;
		if (tuneCount == -1 && !itemTemplate.canTune()) {
			this.tuneCount = 0; // NC made it not tunable
		}
		this.tempering = tempering;
		this.packCount = packCount;
		this.isAmplified = isAmplified;
		this.buffSkill = buffSkill;
		this.rndPlumeBonusValue = rndPlumeBonusValue;
		if (itemTemplate.getStatBonusSetId() != 0 && statBonusId > 0) {
			setBonusStats(statBonusId, false);
		}
		if (fusionedItemTemplate != null) {
			if (fusionedItemTemplate.getStatBonusSetId() != 0 && fusionedItemStatBonusId > 0) {
				setFusionedItemBonusStats(fusionedItemStatBonusId, false);
			}
			if (!itemTemplate.isCanFuse() || !itemTemplate.isTwoHandWeapon() || !fusionedItemTemplate.isCanFuse()
				|| !fusionedItemTemplate.isTwoHandWeapon()) {
				this.fusionedItemTemplate = null;
				this.fusionedItemOptionalSockets = 0;
			}
		}
		updateChargeInfo(charge);
	}

	public int getTempering() {
		return tempering;
	}

	public void setTempering(int tempering) {
		this.tempering = tempering;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	private void updateChargeInfo(int charge) {
		int chargeLevel = calculateMaxChargeLevel();
		if (conditioningInfo == null && chargeLevel > 0)
			conditioningInfo = new ChargeInfo(charge, this);
		// when break fusioned item and second item has conditioned info - set to null
		if (conditioningInfo != null && chargeLevel == 0)
			conditioningInfo = null;
	}

	@Override
	public String getName() {
		return itemTemplate.getName();
	}

	/**
	 * @return itemCreator
	 */
	public String getItemCreator() {
		return itemCreator == null ? "" : itemCreator;
	}

	/**
	 * @param itemCreator
	 *          the itemCreator to set
	 */
	public void setItemCreator(String itemCreator) {
		this.itemCreator = itemCreator;
	}

	public String getItemName() {
		return itemTemplate.getName();
	}

	public int getOptionalSockets() {
		return optionalSockets;
	}

	public void setOptionalSockets(int optionalSockets) {
		this.optionalSockets = optionalSockets;
	}

	public boolean hasOptionalSocket() {
		return optionalSockets != 0;
	}

	public int getFusionedItemOptionalSockets() {
		return fusionedItemOptionalSockets;
	}

	public boolean hasOptionalFusionSocket() {
		return fusionedItemOptionalSockets != 0;
	}

	public void setFusionedItemOptionalSockets(int fusionedItemOptionalSockets) {
		this.fusionedItemOptionalSockets = fusionedItemOptionalSockets;
	}

	public int getEnchantBonus() {
		return enchantBonus;
	}

	public void setEnchantBonus(int enchantBonus) {
		this.enchantBonus = enchantBonus;
	}

	public boolean isStigmaChargeable() {
		return itemTemplate.getStigma() != null && itemTemplate.getStigma().isChargeable();
	}

	/**
	 * @return the itemTemplate
	 */
	public ItemTemplate getItemTemplate() {
		return itemTemplate;
	}

	/**
	 * @return the itemAppearanceTemplate
	 */
	public ItemTemplate getItemSkinTemplate() {
		if (this.itemSkinTemplate == null)
			return this.itemTemplate;
		return this.itemSkinTemplate;
	}

	public void setItemSkinTemplate(ItemTemplate newTemplate) {
		this.itemSkinTemplate = newTemplate;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public boolean isSkinnedItem() {
		return getItemSkinTemplate() != this.itemTemplate;
	}

	/**
	 * @return RGB color value (no alpha channel info) or null if not dyed
	 */
	public Integer getItemColor() {
		return itemColor;
	}

	/**
	 * @param color
	 *          - the item color to set (RGB color value or null to remove dye)
	 */
	public void setItemColor(Integer color) {
		this.itemColor = color == null ? null : color & 0xFFFFFF; // use bit mask to ensure valid value range (no alpha channel support)
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return positive values if not expired, 0 for not expirable, negative for expired
	 */
	public int getColorTimeLeft() {
		if (colorExpireTime == 0)
			return 0;
		return (int) (colorExpireTime - System.currentTimeMillis() / 1000);
	}

	public int getColorExpireTime() {
		return colorExpireTime;
	}

	public void setColorExpireTime(int dyeRemainsUntil) {
		this.colorExpireTime = dyeRemainsUntil;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return the itemCount Number of this item in stack. Should be not more than template maxstackcount ?
	 */
	public long getItemCount() {
		return itemCount;
	}

	public long getFreeCount() {
		return itemTemplate.getMaxStackCount() - itemCount;
	}

	/**
	 * @param itemCount
	 *          the itemCount to set
	 */
	public void setItemCount(long itemCount) {
		this.itemCount = itemCount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * This method should be called ONLY from Storage class In all other ways it is not guaranteed to be udpated in a regular update service It is
	 * allowed to use this method for newly created items which are not yet in any storage
	 *
	 * @param count
	 */
	public long increaseItemCount(long count) {
		if (count <= 0) {
			return 0;
		}
		long cap = itemTemplate.getMaxStackCount();
		long addCount = this.itemCount + count > cap ? cap - this.itemCount : count;
		if (addCount != 0) {
			this.itemCount += addCount;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
		return count - addCount;
	}

	/**
	 * This method should be called ONLY from Storage class In all other ways it is not guaranteed to be udpated in a regular update service It is
	 * allowed to use this method for newly created items which are not yet in any storage
	 *
	 * @param count
	 */
	public long decreaseItemCount(long count) {
		if (count <= 0) {
			return 0;
		}
		long removeCount = count >= itemCount ? itemCount : count;
		this.itemCount -= removeCount;
		if (itemCount == 0 && !this.itemTemplate.isKinah()) {
			setPersistentState(PersistentState.DELETED);
		} else {
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
		return count - removeCount;
	}

	/**
	 * @return the isEquipped
	 */
	public boolean isEquipped() {
		return isEquipped;
	}

	/**
	 * @param isEquipped
	 *          the isEquipped to set
	 */
	public void setEquipped(boolean isEquipped) {
		this.isEquipped = isEquipped;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return the equipmentSlot Equipment slot can be of 2 types - one is the ItemSlot enum type if equipped, second - is position in cube FIXME:
	 *         That's the biggest nonsense!!! Slot value is Q, while position in Cube is H [RR]
	 */
	public long getEquipmentSlot() {
		return equipmentSlot;
	}

	/**
	 * @param equipmentSlot
	 *          the equipmentSlot to set
	 */
	public void setEquipmentSlot(long equipmentSlot) {
		this.equipmentSlot = equipmentSlot;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * This method should be used to lazy initialize empty manastone list
	 *
	 * @return the itemStones
	 */
	public Set<ManaStone> getItemStones() {
		if (manaStones == null)
			this.manaStones = itemStonesCollection();
		return manaStones;
	}

	/**
	 * This method should be used to lazy initialize empty manastone list
	 *
	 * @return the itemStones
	 */
	public Set<ManaStone> getFusionStones() {
		if (fusionStones == null)
			this.fusionStones = itemStonesCollection();
		return fusionStones;
	}

	public int getFusionStonesSize() {
		if (fusionStones == null)
			return 0;
		return fusionStones.size();
	}

	public int getItemStonesSize() {
		if (manaStones == null)
			return 0;
		return manaStones.size();
	}

	private Set<ManaStone> itemStonesCollection() {
		return new TreeSet<>(new Comparator<ManaStone>() {

			@Override
			public int compare(ManaStone o1, ManaStone o2) {
				if (o1.getSlot() == o2.getSlot())
					return 0;
				return o1.getSlot() > o2.getSlot() ? 1 : -1;
			}

		});
	}

	/**
	 * Check manastones without initialization
	 *
	 * @return
	 */
	public boolean hasManaStones() {
		return manaStones != null && manaStones.size() > 0;
	}

	/**
	 * Check fusionstones without initialization
	 *
	 * @return
	 */
	public boolean hasFusionStones() {
		return fusionStones != null && fusionStones.size() > 0;
	}

	public boolean hasIdianStone() {
		return idianStone != null;
	}

	public boolean hasGodStone() {
		return godStone != null;
	}

	public GodStone getGodStone() {
		return godStone;
	}

	public int getGodStoneId() {
		return godStone == null ? 0 : godStone.getItemId();
	}

	public void addGodStone(int itemId) {
		addGodStone(itemId, 0);
	}

	public void addGodStone(int itemId, int activatedCount) {
		GodstoneInfo godstoneInfo = DataManager.ITEM_DATA.getItemTemplate(itemId).getGodstoneInfo();
		if (godstoneInfo == null) {
			log.warn("Item " + itemId + " has no godstone info");
			return;
		}
		if (godStone != null)
			setGodStone(null);
		godStone = new GodStone(this, activatedCount, itemId, godstoneInfo, PersistentState.NEW);
	}

	public void setGodStone(GodStone godStone) {
		if (godStone == null) {
			this.godStone.setPersistentState(PersistentState.DELETED);
			ItemStoneListDAO.storeGodStones(this.godStone);
		}
		this.godStone = godStone;
	}

	/**
	 * @return the echantLevel
	 */
	public int getEnchantLevel() {
		return enchantLevel;
	}

	/**
	 * @param enchantLevel
	 *          the echantLevel to set
	 */
	public void setEnchantLevel(int enchantLevel) {
		this.enchantLevel = enchantLevel;
		if (enchantLevel > 0)
			removeRemainingTuningCountIfPossible();
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return the persistentState
	 */
	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * Possible changes: NEW -> UPDATED NEW -> UPDATE_REQURIED UPDATE_REQUIRED -> DELETED UPDATE_REQUIRED -> UPDATED UPDATED -> DELETED UPDATED ->
	 * UPDATE_REQUIRED
	 *
	 * @param persistentState
	 *          the persistentState to set
	 */
	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

	public void setItemLocation(int storageType) {
		this.itemLocation = storageType;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getItemLocation() {
		return itemLocation;
	}

	public int getItemMask() {
		return itemTemplate.getMask();
	}

	public boolean isSoulBound() {
		return isSoulBound;
	}

	private boolean isSoulBound(Player player) {
		if (player.hasPermission(MembershipConfig.DISABLE_SOULBIND)) {
			return false;
		} else
			return isSoulBound;
	}

	public void setSoulBound(boolean isSoulBound) {
		this.isSoulBound = isSoulBound;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public EquipType getEquipmentType() {
		if (itemTemplate.isStigma())
			return EquipType.STIGMA;
		return itemTemplate.getEquipmentType();
	}

	public int getItemId() {
		return itemTemplate.getTemplateId();
	}

	public String getL10n() {
		return itemTemplate.getL10n();
	}

	public boolean hasFusionedItem() {
		return fusionedItemTemplate != null;
	}

	public ItemTemplate getFusionedItemTemplate() {
		return fusionedItemTemplate;
	}

	public int getFusionedItemId() {
		return fusionedItemTemplate != null ? fusionedItemTemplate.getTemplateId() : 0;
	}

	public void setFusionedItem(Item fusionedItem) {
		if (fusionedItem == null)
			setFusionedItem(null, 0, 0);
		else
			setFusionedItem(fusionedItem.getItemTemplate(), fusionedItem.getBonusStatsId(), fusionedItem.getOptionalSockets());
	}

	public void setFusionedItem(ItemTemplate template, int bonusStatsId, int optionalSockets) {
		removeAllFusionStones();
		fusionedItemTemplate = template;
		setFusionedItemBonusStats(bonusStatsId, false);
		setFusionedItemOptionalSockets(optionalSockets);
		updateChargeInfo(0);
		if (template != null)
			removeRemainingTuningCountIfPossible();
	}

	private void removeAllFusionStones() {
		if (!hasFusionStones())
			return;
		for (ManaStone ms : fusionStones)
			ms.setPersistentState(PersistentState.DELETED);
		ItemStoneListDAO.storeFusionStone(fusionStones);
		fusionStones.clear();
	}

	public int getSockets(boolean isFusionItem) {
		int numSockets;
		if (itemTemplate.isWeapon() || itemTemplate.isArmor()) {
			if (isFusionItem) {
				ItemTemplate fusedTemp = getFusionedItemTemplate();
				if (fusedTemp == null)
					return 0;
				numSockets = fusedTemp.getManastoneSlots() + getFusionedItemOptionalSockets();
			} else {
				numSockets = getItemTemplate().getManastoneSlots() + getOptionalSockets();
			}
			return Math.min(numSockets, MAX_BASIC_STONES);
		}
		return 0;
	}

	public int getItemMask(Player player) {
		return checkConfig(player, itemTemplate.getMask());
	}

	private int checkConfig(Player player, int mask) {
		int newMask = mask;
		if (player.hasPermission(MembershipConfig.STORE_WH_ALL)) {
			newMask = newMask | ItemMask.STORABLE_IN_WH;
		}
		if (player.hasPermission(MembershipConfig.STORE_AWH_ALL)) {
			newMask = newMask | ItemMask.STORABLE_IN_AWH;
		}
		if (player.hasPermission(MembershipConfig.STORE_LWH_ALL)) {
			newMask = newMask | ItemMask.STORABLE_IN_LWH;
		}
		if (player.hasPermission(MembershipConfig.TRADE_ALL)) {
			newMask = newMask | ItemMask.TRADEABLE;
		}
		if (player.hasPermission(MembershipConfig.REMODEL_ALL)) {
			newMask = newMask | ItemMask.REMODELABLE;
		}

		return newMask;
	}

	public boolean isStorableinWarehouse(Player player) {
		return (getItemMask(player) & ItemMask.STORABLE_IN_WH) == ItemMask.STORABLE_IN_WH;
	}

	public boolean isStorableinAccWarehouse(Player player) {
		return (getItemMask(player) & ItemMask.STORABLE_IN_AWH) == ItemMask.STORABLE_IN_AWH && !isSoulBound(player);
	}

	public boolean isStorableinLegWarehouse(Player player) {
		return (getItemMask(player) & ItemMask.STORABLE_IN_LWH) == ItemMask.STORABLE_IN_LWH && !isSoulBound(player);
	}

	public boolean isTradeable(Player player) {
		return (getItemMask(player) & ItemMask.TRADEABLE) == ItemMask.TRADEABLE && !isSoulBound(player);
	}

	public boolean isLegionTradeable(Player player, Player partner) {
		if ((getItemMask(player) & ItemMask.LEGION_TRADEABLE) != ItemMask.LEGION_TRADEABLE || isSoulBound(player))
			return false;
		if (player.getLegion() == null || partner.getLegion() == null)
			return false;
		return player.getLegion().getLegionId() == partner.getLegion().getLegionId();
	}

	public boolean isRemodelable(Player player) {
		return (getItemMask(player) & ItemMask.REMODELABLE) == ItemMask.REMODELABLE;
	}

	public boolean isSellable() {
		return (getItemMask() & ItemMask.SELLABLE) == ItemMask.SELLABLE;
	}

	public boolean canApExtract() {
		return (getItemMask() & ItemMask.CAN_AP_EXTRACT) == ItemMask.CAN_AP_EXTRACT;
	}

	public boolean canSocketGodstone() {
		return (getItemMask() & ItemMask.CAN_PROC_ENCHANT) == ItemMask.CAN_PROC_ENCHANT;
	}

	/**
	 * @return Returns the expireTime.
	 */
	@Override
	public int getExpireTime() {
		return expireTime;
	}

	/**
	 * @return Returns the temporaryExchangeTime.
	 */
	public int getTemporaryExchangeTime() {
		return temporaryExchangeTime;
	}

	public int getTemporaryExchangeTimeRemaining() {
		if (temporaryExchangeTime == 0)
			return 0;
		return temporaryExchangeTime - (int) (System.currentTimeMillis() / 1000);
	}

	/**
	 * @param temporaryExchangeTime
	 *          The temporaryExchangeTime to set.
	 */
	public void setTemporaryExchangeTime(int temporaryExchangeTime) {
		this.temporaryExchangeTime = temporaryExchangeTime;
	}

	@Override
	public void onExpire(Player player) {
		if (isEquipped())
			player.getEquipment().unEquipItem(getObjectId());

		for (StorageType i : StorageType.values()) {
			if (i == StorageType.LEGION_WAREHOUSE)
				continue;
			IStorage storage = player.getStorage(i.getId());

			if (storage != null && storage.getItemByObjId(getObjectId()) != null) {
				storage.delete(this);
				switch (i) {
					case CUBE:
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DELETE_CASH_ITEM_BY_TIMEOUT(getL10n()));
						break;
					case ACCOUNT_WAREHOUSE:
					case REGULAR_WAREHOUSE:
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DELETE_CASH_ITEM_BY_TIMEOUT_IN_WAREHOUSE(getL10n()));
						break;
				}
			}
		}
	}

	@Override
	public void onBeforeExpire(Player player, int remainingMinutes) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CASH_ITEM_TIME_LEFT(getL10n(), remainingMinutes));
	}

	public void setRepurchasePrice(long price) {
		repurchasePrice = price;
	}

	public long getRepurchasePrice() {
		return repurchasePrice;
	}

	public int getActivationCount() {
		return activationCount;
	}

	public void setActivationCount(int activationCount) {
		this.activationCount = activationCount;
	}

	public ChargeInfo getConditioningInfo() {
		return conditioningInfo;
	}

	public int getChargePoints() {
		return conditioningInfo != null ? conditioningInfo.getChargePoints() : 0;
	}

	public int getChargeLevel() {
		if (getChargePoints() == 0)
			return 0;
		return getChargePoints() > ChargeInfo.LEVEL1 ? 2 : 1;
	}

	/**
	 * Calculate charge level based on main item and fusioned item
	 */
	public int calculateMaxChargeLevel() {
		int chargeLevel = 0;
		if (getImprovement() != null)
			chargeLevel = getImprovement().getLevel();

		int fusionedChargeLevel = 0;
		if (hasFusionedItem() && fusionedItemTemplate.getImprovement() != null)
			fusionedChargeLevel = fusionedItemTemplate.getImprovement().getLevel();
		return Math.max(chargeLevel, fusionedChargeLevel);
	}

	/*
	 * Check for disabled charge levels due to recommend rank restriction
	 */
	public int calculateAvailableChargeLevel(Player player) {
		int maxAvailableChargeLevel = calculateMaxChargeLevel();
		ItemUseLimits limits = hasFusionedItem() && fusionedItemTemplate.getLevel() > itemTemplate.getLevel()
			? fusionedItemTemplate.getUseLimits()
			: itemTemplate.getUseLimits();
		if (limits.getRecommendRank() > 0) {
			int rankLevelDiff = Math.max(0, limits.getRecommendRank() - player.getAbyssRank().getRank().getId());
			maxAvailableChargeLevel -= rankLevelDiff;
		}
		return Math.max(0, maxAvailableChargeLevel);
	}

	public Improvement getImprovement() {
		if (itemTemplate.getImprovement() != null)
			return itemTemplate.getImprovement();
		else if (hasFusionedItem() && fusionedItemTemplate.getImprovement() != null)
			return fusionedItemTemplate.getImprovement();
		return null;
	}

	public List<StatFunction> getCurrentModifiers() {
		if (currentModifiers == null)
			currentModifiers = new ArrayList<>();
		return currentModifiers;
	}

	public void setCurrentModifiers(List<StatFunction> currentModifiers) {
		getCurrentModifiers().clear();
		getCurrentModifiers().addAll(currentModifiers);
	}

	public IdianStone getIdianStone() {
		return idianStone;
	}

	public void setIdianStone(IdianStone idianStone) {
		this.idianStone = idianStone;
	}

	public int getBonusStatsId() {
		return bonusStatsEffect == null ? 0 : bonusStatsEffect.getStatBonusId();
	}

	public RandomBonusEffect getBonusStatsEffect() {
		return bonusStatsEffect;
	}

	/**
	 * Must only be called while the item is unequipped, otherwise the old stats will remain active.
	 */
	public void setBonusStats(int statBonusId, boolean validate) {
		if (validate && isEquipped)
			log.warn(getItemId() + " was equipped while switching bonus stats from " + getBonusStatsId() + " to " + statBonusId,
				new IllegalStateException());
		if (statBonusId == 0)
			bonusStatsEffect = null;
		else
			bonusStatsEffect = new RandomBonusEffect(StatBonusType.INVENTORY, itemTemplate.getStatBonusSetId(), statBonusId);
	}

	public int getTuneCount() {
		return tuneCount;
	}

	public void setTuneCount(int tuneCount) {
		this.tuneCount = tuneCount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public void removeRemainingTuningCountIfPossible() {
		if (isIdentified() && itemTemplate.getMaxTuneCount() > 0 && tuneCount != itemTemplate.getMaxTuneCount())
			setTuneCount(itemTemplate.getMaxTuneCount());
	}

	/**
	 * @return False if the item must be identified (tuned) before it can be equipped (identification can be made without a tuning scroll)
	 */
	public boolean isIdentified() {
		return tuneCount != -1;
	}

	public int getFusionedItemBonusStatsId() {
		return fusionedItemBonusStatsEffect == null ? 0 : fusionedItemBonusStatsEffect.getStatBonusId();
	}

	public RandomBonusEffect getFusionedItemBonusStatsEffect() {
		return fusionedItemBonusStatsEffect;
	}

	/**
	 * Must only be called while the item is unequipped, otherwise the old stats will remain active.
	 */
	public void setFusionedItemBonusStats(int statBonusId, boolean validate) {
		if (validate && isEquipped)
			log.warn(getItemId() + " was equipped while switching fusioned bonus stats from " + getFusionedItemBonusStatsId() + " to " + statBonusId,
				new IllegalStateException());
		if (statBonusId == 0)
			fusionedItemBonusStatsEffect = null;
		else
			fusionedItemBonusStatsEffect = new RandomBonusEffect(StatBonusType.INVENTORY, fusionedItemTemplate.getStatBonusSetId(), statBonusId);
	}

	public void setTemperingEffect(TemperingEffect temperingEffect) {
		this.temperingEffect = temperingEffect;
	}

	public TemperingEffect getTemperingEffect() {
		return temperingEffect;
	}

	public void setEnchantEffect(EnchantEffect enchantEffect) {
		this.enchantEffect = enchantEffect;
	}

	public EnchantEffect getEnchantEffect() {
		return enchantEffect;
	}

	public int getPackCount() {
		return packCount;
	}

	public void setPackCount(int packCount) {
		this.packCount = packCount;
	}

	public int getMaxEnchantLevel() {
		return this.getItemTemplate().getMaxEnchantLevel() + this.getEnchantBonus();
	}

	public int getItemEnchantParam() {
		if (this.getItemTemplate().isWeapon()) {
			if (this.getEnchantLevel() >= 5 && this.getEnchantLevel() < 10)
				return 1;
			else if (this.getEnchantLevel() >= getMaxEnchantLevel() && this.getEnchantLevel() < 20)
				return 2;
			else if (this.getEnchantLevel() >= 20)
				return 20;
		} else {
			if (this.getTempering() >= 5 && this.getTempering() < 10)
				return 10;
			else if (this.getTempering() >= 10)
				return 20;
		}
		return this.getTempering();
	}

	public boolean isAmplified() {
		return isAmplified;
	}

	public void setAmplified(boolean isAmplified) {
		this.isAmplified = isAmplified;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getBuffSkill() {
		return buffSkill;
	}

	public void setBuffSkill(int buffSkill) {
		this.buffSkill = buffSkill;
	}

	public int getRndPlumeBonusValue() {
		return rndPlumeBonusValue;
	}

	public void setRndPlumeBonusValue(int rndPlumeBonusValue) {
		this.rndPlumeBonusValue = rndPlumeBonusValue;
	}

	public PendingTuneResult getPendingTuneResult() {
		return pendingTuneResult;
	}

	public void setPendingTuneResult(PendingTuneResult pendingTuneResult) {
		this.pendingTuneResult = pendingTuneResult;
	}

	@Override
	public String toString() {
		return "Item [getItemId()=" + getItemId() + ", getObjectId()=" + getObjectId() + ", itemCount=" + itemCount + ", itemColor=" + itemColor
			+ ", colorExpireTime=" + colorExpireTime + ", itemCreator=" + itemCreator + ", itemSkinId=" + getItemSkinTemplate().getTemplateId()
			+ ", getFusionedItemId()=" + getFusionedItemId() + ", isEquipped=" + isEquipped + ", manaStones=" + manaStones + ", fusionStones="
			+ fusionStones + ", optionalSockets=" + optionalSockets + ", fusionedItemOptionalSockets=" + fusionedItemOptionalSockets + ", getGodStoneId()="
			+ getGodStoneId() + ", isSoulBound=" + isSoulBound + ", itemLocation=" + itemLocation + ", enchantLevel=" + enchantLevel + ", enchantBonus="
			+ enchantBonus + ", expireTime=" + expireTime + ", temporaryExchangeTime=" + temporaryExchangeTime + ", repurchasePrice=" + repurchasePrice
			+ ", activationCount=" + activationCount + ", bonusNumber=" + getBonusStatsId() + ", tuneCount=" + tuneCount + ", packCount=" + packCount
			+ ", tempering=" + tempering + ", isAmplified=" + isAmplified + ", buffSkill=" + buffSkill + ", rndPlumeBonusValue=" + rndPlumeBonusValue
			+ ", getChargePoints()=" + getChargePoints() + "]";
	}
}
