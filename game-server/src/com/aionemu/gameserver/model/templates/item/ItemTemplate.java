package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.items.ItemMask;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.model.templates.item.enums.ArmorType;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Luno modified by ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "", name = "ItemTemplate")
public class ItemTemplate extends VisibleObjectTemplate {

	@XmlAttribute(name = "id", required = true)
	@XmlID
	private String id;
	@XmlElement(name = "modifiers", required = false)
	protected ModifiersTemplate modifiers;
	@XmlElement(name = "actions", required = false)
	protected ItemActions actions;
	@XmlAttribute(name = "mask")
	private int mask;
	@XmlAttribute(name = "weapon_boost")
	private int weaponBoost;
	@XmlAttribute(name = "price")
	private int price;
	@XmlAttribute(name = "max_stack_count")
	private int maxStackCount = 1;
	@XmlAttribute(name = "item_group")
	private ItemGroup itemGroup = ItemGroup.NONE;
	@XmlAttribute(name = "pack_count")
	private int packCount;
	@XmlAttribute(name = "level")
	private int level;
	@XmlAttribute(name = "quality")
	private ItemQuality itemQuality;
	@XmlAttribute(name = "item_type")
	private ItemType itemType;
	@XmlAttribute(name = "attack_type")
	private ItemAttackType attackType;
	@XmlAttribute(name = "attack_gap")
	private float attackGap;
	@XmlAttribute(name = "desc")
	private String description;
	@XmlAttribute(name = "option_slot_bonus")
	private int optionSlotBonus;
	@XmlAttribute(name = "rnd_bonus")
	private int rnd_bonus = 0;
	@XmlAttribute(name = "rnd_count")
	private int rnd_count = -1;
	@XmlAttribute(name = "bonus_apply")
	private String bonusApply;// enum
	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;
	private int itemId;
	@XmlAttribute(name = "return_world")
	private int returnWorldId;
	@XmlAttribute(name = "return_alias")
	private String returnAlias;
	@XmlElement(name = "godstone")
	private GodstoneInfo godstoneInfo;
	@XmlElement(name = "stigma")
	private Stigma stigma;
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "restrict")
	private String restrict;
	@XmlAttribute(name = "restrict_max")
	private String restrictMax;
	@XmlTransient
	private int[] restricts;
	@XmlTransient
	private byte[] restrictsMax;
	@XmlAttribute(name = "m_slots")
	private int manastoneSlots;
	@XmlAttribute(name = "s_slots")
	private int specialSlots;
	@XmlAttribute(name = "max_enchant")
	private int maxEnchant;
	@XmlAttribute(name = "max_enchant_bonus")
	private int maxEnchantBonus;
	@XmlAttribute(name = "enchant_type")
	private int enchantType;
	@XmlAttribute(name = "max_tampering")
	private int maxTampering;
	@XmlAttribute(name = "temp_exchange_time")
	protected int temExchangeTime;
	@XmlAttribute(name = "expire_time")
	protected int expireTime;
	@XmlElement(name = "weapon_stats")
	protected WeaponStats weaponStats;
	@XmlAttribute(name = "activate_target")
	private String activationTarget;
	@XmlAttribute(name = "tempering_name")
	private String temperingName;
	@XmlAttribute(name = "enchant_name")
	private String enchantName;
	@XmlAttribute(name = "subtype_prefix")
	private String subtypePrefix;
	@XmlAttribute(name = "activate_count")
	private int activationCount;
	@XmlAttribute(name = "activate_combat")
	private boolean activateCombat;
	@XmlAttribute(name = "robot")
	private Integer robotId;
	@XmlElement(name = "tradein_list")
	protected TradeinList tradeinList;
	@XmlElement(name = "acquisition")
	private Acquisition acquisition;
	@XmlElement(name = "disposition")
	private Disposition disposition;
	@XmlElement(name = "improve")
	private Improvement improvement;
	@XmlElement(name = "uselimits")
	private ItemUseLimits useLimits = new ItemUseLimits();
	@XmlElement(name = "inventory")
	private ExtraInventory extraInventory;
	@XmlElement(name = "idian")
	private Idian idianAction;
	@XmlAttribute(name = "can_exceed_enchant")
	private boolean canExceedEnchant;
	@XmlAttribute(name = "exceed_enchant_skill")
	private ExceedEnchantSkillSetType exceedEnchantSkill;

	private static final WeaponStats emptyWeaponStats = new WeaponStats();
	@XmlTransient
	private boolean isQuestUpdateItem;

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		setItemId(Integer.parseInt(id));
		String[] parts = restrict.split(",");
		restricts = new int[17];
		for (int i = 0; i < parts.length; i++) {
			restricts[i] = Integer.parseInt(parts[i]);
		}
		if (restrictMax != null) {
			String[] partsMax = restrictMax.split(",");
			restrictsMax = new byte[17];
			for (int i = 0; i < partsMax.length; i++) {
				restrictsMax[i] = Byte.parseByte(partsMax[i]);
			}
		}
		if (weaponStats == null)
			weaponStats = emptyWeaponStats;

		// check if it can be randomized
		if (getItemSlot() == 0)
			rnd_count = 0;
		else if (rnd_count == -1) {
			if (maxEnchantBonus == 0 && optionSlotBonus == 0 && rnd_bonus == 0)
				rnd_count = 0;
		}
	}

	public int getMask() {
		return mask;
	}

	public long getItemSlot() {
		return itemGroup.getSlots();
	}

	/**
	 * @param playerClass
	 * @return
	 */
	public boolean isClassSpecific(PlayerClass playerClass) {
		boolean related = restricts[playerClass.ordinal()] > 0;
		if (!related && !playerClass.isStartingClass()) {
			related = restricts[PlayerClass.getStartingClassFor(playerClass).ordinal()] > 0;
		}
		return related;
	}

	/**
	 * @param playerClass
	 * @param level
	 * @return
	 */
	public int getRequiredLevel(PlayerClass playerClass) {
		int requiredLevel = restricts[playerClass.ordinal()];
		if (requiredLevel == 0)
			return -1;
		else
			return requiredLevel;
	}

	public byte getMaxLevelRestrict(PlayerClass playerClass) {
		if (restrictMax != null) {
			return restrictsMax[playerClass.getClassId()];
		}
		return 0;
	}

	public List<StatFunction> getModifiers() {
		if (modifiers != null) {
			return modifiers.getModifiers();
		}
		return null;
	}

	public ItemActions getActions() {
		return actions;
	}

	public ItemSubType getItemSubType() {
		return itemGroup.getItemSubType();
	}

	public EquipType getEquipmentType() {
		return itemGroup.getEquipType();
	}

	public int getPrice() {
		return price;
	}

	public int getLevel() {
		return level;
	}

	public ItemQuality getItemQuality() {
		return itemQuality;
	}

	public ItemType getItemType() {
		return itemType;
	}

	@Override
	public int getNameId() {
		try {
			int val = Integer.parseInt(description);
			return val;
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public long getMaxStackCount() {
		if (isKinah()) {
			if (CustomConfig.ENABLE_KINAH_CAP) {
				return CustomConfig.KINAH_CAP_VALUE;
			} else {
				return Long.MAX_VALUE;
			}
		}
		return maxStackCount;
	}

	public ItemAttackType getAttackType() {
		return attackType;
	}

	public float getAttackGap() {
		return attackGap;
	}

	public int getOptionSlotBonus() {
		return optionSlotBonus;
	}

	public String getBonusApply() {
		return bonusApply;
	}

	public boolean isNoEnchant() {
		return (getMask() & ItemMask.NO_ENCHANT) == ItemMask.NO_ENCHANT;
	}

	public boolean isItemDyePermitted() {
		return (getMask() & ItemMask.DYEABLE) == ItemMask.DYEABLE;
	}

	public Race getRace() {
		return race;
	}

	public int getWeaponBoost() {
		return weaponBoost;
	}

	public boolean isWeapon() {
		return getEquipmentType() == EquipType.WEAPON;
	}

	public boolean isArmor() {
		return getEquipmentType() == EquipType.ARMOR;
	}

	public boolean isKinah() {
		return itemId == ItemId.KINAH.value();
	}

	public boolean isStigma() {
		return stigma != null;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return id of the associated ItemSetTemplate or null if none
	 */
	public ItemSetTemplate getItemSet() {
		return DataManager.ITEM_SET_DATA.getItemSetTemplateByItemId(itemId);
	}

	/**
	 * Checks if the ItemTemplate belongs to an item set
	 */
	public boolean isItemSet() {
		return getItemSet() != null;
	}

	public GodstoneInfo getGodstoneInfo() {
		return godstoneInfo;
	}

	@Override
	public String getName() {
		return name != null ? name : StringUtils.EMPTY;
	}

	@Override
	public int getTemplateId() {
		return itemId;
	}

	public int getReturnWorldId() {
		return returnWorldId;
	}

	public String getReturnAlias() {
		return returnAlias;
	}

	public Stigma getStigma() {
		return stigma;
	}

	public int getManastoneSlots() {
		return manastoneSlots;
	}

	public int getSpecialSlots() {
		return specialSlots;
	}

	public int getMaxEnchantLevel() {
		return maxEnchant;
	}

	public int getMaxEnchantBonus() {
		return maxEnchantBonus;
	}

	public boolean hasLimitOne() {
		return (getMask() & ItemMask.LIMIT_ONE) == ItemMask.LIMIT_ONE;
	}

	public boolean isTradeable() {
		return (getMask() & ItemMask.TRADEABLE) == ItemMask.TRADEABLE;
	}

	public boolean isCanFuse() {
		return (getMask() & ItemMask.CAN_COMPOSITE_WEAPON) == ItemMask.CAN_COMPOSITE_WEAPON;
	}

	public boolean canExtract() {
		return (getMask() & ItemMask.CAN_SPLIT) == ItemMask.CAN_SPLIT;
	}

	public boolean isSoulBound() {
		return (getMask() & ItemMask.SOUL_BOUND) == ItemMask.SOUL_BOUND;
	}

	public boolean isBreakable() {
		return (getMask() & ItemMask.BREAKABLE) == ItemMask.BREAKABLE;
	}

	public boolean isDeletable() {
		return (getMask() & ItemMask.DELETABLE) == ItemMask.DELETABLE;
	}

	public boolean isCanPolish() {
		return (getMask() & ItemMask.CAN_POLISH) == ItemMask.CAN_POLISH;
	}

	public boolean canTune() {
		return rnd_count != 0;
	}

	public boolean isTwoHandWeapon() {
		if (!isWeapon())
			return false;
		return getItemSubType() == ItemSubType.TWO_HAND;
	}

	public int getTempExchangeTime() {
		return temExchangeTime;
	}

	public int getEnchantType() {
		return enchantType;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public final WeaponStats getWeaponStats() {
		return weaponStats;
	}

	public int getActivationCount() {
		return activationCount;
	}

	/**
	 * -1 if no id, can be values 0, 1, 2
	 */
	public int getExtraInventoryId() {
		if (extraInventory == null) {
			return -1;
		}
		return extraInventory.getId();
	}

	public void modifyMask(boolean apply, int filter) {
		if (apply)
			mask |= filter;
		else
			mask &= ~filter;
	}

	public boolean isStackable() {
		return this.maxStackCount > 1;
	}

	public boolean hasAreaRestriction() {
		return useLimits.getUseArea() != null;
	}

	public ZoneName getUseArea() {
		return useLimits.getUseArea();
	}

	/**
	 * @return the tradeinList
	 */
	public TradeinList getTradeinList() {
		return tradeinList;
	}

	/**
	 * @return the acquisition
	 */
	public Acquisition getAcquisition() {
		return acquisition;
	}

	/**
	 * @return the rnd_bonus, 0 if no bonus exists
	 */
	public int getRandomBonusId() {
		return rnd_bonus;
	}

	/**
	 * @return the rnd_count, 0 if can not be randomized
	 */
	public int getRandomBonusCount() {
		return rnd_count;
	}

	/**
	 * @return the conditioning
	 */
	public Improvement getImprovement() {
		return improvement;
	}

	/**
	 * @return the useLimits
	 */
	public ItemUseLimits getUseLimits() {
		return useLimits;
	}

	public Disposition getDisposition() {
		return disposition;
	}

	public int getOwnershipWorld() {
		return useLimits.getOwnershipWorld();
	}

	public boolean isCloth() {
		// not sure about LT_HEAD and CL_HEAD, check in retail
		return isArmor() && (itemGroup.getArmorType() != ArmorType.ACCESSORY && getItemGroup() != ItemGroup.BELT || itemGroup == ItemGroup.HEAD);
	}

	public boolean isPotion() {
		return itemId >= 162000000 && itemId < 163000000;
	}

	public boolean isQuestUpdateItem() {
		return isQuestUpdateItem;
	}

	public void setQuestUpdateItem(boolean value) {
		this.isQuestUpdateItem = value;
	}

	public Idian getIdianAction() {
		return idianAction;
	}

	public boolean isCombinationItem() {
		return getItemGroup() == ItemGroup.COMBINATION;
	}

	public boolean isEnchantmentStone() {
		return getItemGroup() == ItemGroup.ENCHANTMENT;
	}

	public ItemActivationTarget getActivationTarget() {
		if (getActivationRace() == null)
			return ItemActivationTarget.getFromString(activationTarget);
		return ItemActivationTarget.NONE;
	}

	public Race getActivationRace() {
		return Race.getRaceByString(activationTarget);
	}

	public boolean isCombatActivated() {
		return activateCombat;
	}

	public Integer getRobotId() {
		if (robotId == null)
			return 0;
		return robotId;
	}

	public int getPackCount() {
		return packCount;
	}

	public int getMaxTampering() {
		return maxTampering;
	}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public int[] getRequiredSkills() {
		return itemGroup.getRequiredSkills(subtypePrefix);
	}

	public String getTemperingName() {
		return temperingName;
	}
	
	public String getEnchantName() {
		return enchantName;
	}

	public String getSubtypePrefix() {
		return subtypePrefix;
	}

	public boolean canExceedEnchant() {
		return canExceedEnchant;
	}

	public ExceedEnchantSkillSetType getExceedEnchantSkill() {
		return exceedEnchantSkill;
	}

}
