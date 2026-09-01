package com.aionemu.gameserver.model.templates.item.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.items.ItemSlot;

/**
 * @author xTz
 */
@XmlType(name = "item_group")
@XmlEnum
public enum ItemGroup {
	NONE,
	NOWEAPON(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	SWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, true),
	GREATSWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	EXTRACT_SWORD(0, ItemSubType.NONE),
	DAGGER(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, true),
	MACE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, true),
	ORB(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	SPELLBOOK(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	POLEARM(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	STAFF(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	BOW(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	HARP(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	GUN(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, true),
	CANNON(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	KEYBLADE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, true),
	SHIELD(ItemSlot.SUB_HAND.getSlotIdMask(), ItemSubType.SHIELD, true),
	
	TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ALL_ARMOR, true),
	GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ALL_ARMOR, true),
	SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ALL_ARMOR, true),
	PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ALL_ARMOR, true),
	SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ALL_ARMOR, true),
	RB_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ROBE, true),
	RB_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ROBE, true),
	RB_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ROBE, true),
	RB_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ROBE, true),
	RB_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ROBE, true),
	CL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CLOTHES, true),
	CL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CLOTHES, true),
	CL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CLOTHES, true),
	CL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CLOTHES, true),
	CL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CLOTHES, true),
	LT_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.LEATHER, true),
	LT_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.LEATHER, true),
	LT_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.LEATHER, true),
	LT_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.LEATHER, true),
	LT_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.LEATHER, true),
	CH_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CHAIN, true),
	CH_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CHAIN, true),
	CH_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CHAIN, true),
	CH_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CHAIN, true),
	CH_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CHAIN, true),
	PL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.PLATE, true),
	PL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.PLATE, true),
	PL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.PLATE, true),
	PL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.PLATE, true),
	PL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.PLATE, true),
	
	EARRING(ItemSlot.EARRINGS_LEFT.getSlotIdMask() | ItemSlot.EARRINGS_RIGHT.getSlotIdMask(), ArmorType.ACCESSORY),
	RING(ItemSlot.RING_LEFT.getSlotIdMask() | ItemSlot.RING_RIGHT.getSlotIdMask(), ArmorType.ACCESSORY),
	NECKLACE(ItemSlot.NECKLACE.getSlotIdMask(), ArmorType.ACCESSORY),
	BELT(ItemSlot.WAIST.getSlotIdMask(), ArmorType.ACCESSORY),
	WING(ItemSlot.WINGS.getSlotIdMask(), ItemSubType.WING),
	PLUME(ItemSlot.PLUME.getSlotIdMask(), ItemSubType.PLUME),

	HEAD(ItemSlot.HELMET.getSlotIdMask(), ArmorType.ACCESSORY),
	LT_HEADS(ItemSlot.HELMET.getSlotIdMask(), ItemSubType.LEATHER),
	CL_HEADS(ItemSlot.HELMET.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_MULTISLOT(ItemSlot.TORSO.getSlotIdMask() | ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_SHIELD(ItemSlot.SUB_HAND.getSlotIdMask(), ArmorType.ACCESSORY),
	
	POWER_SHARDS(ItemSlot.POWER_SHARD_RIGHT.getSlotIdMask() | ItemSlot.POWER_SHARD_LEFT.getSlotIdMask(), ArmorType.ACCESSORY),
	STIGMA(ItemSlot.ALL_STIGMA.getSlotIdMask(), ItemSubType.STIGMA),
	// other
	ARROW(0, ItemSubType.ARROW),
	NPC_MACE(ItemSlot.MAIN_HAND.getSlotIdMask(), ItemSubType.ONE_HAND), // keep it above TOOLHOES, for search picking it up
	TOOLRODS(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	TOOLHOES(ItemSlot.MAIN_HAND.getSlotIdMask(), ItemSubType.ONE_HAND),
	TOOLPICKS(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	// non equip
	MANASTONE,
	SPECIAL_MANASTONE,
	RECIPE,
	ENCHANTMENT,
	PACK_SCROLL,
	FLUX,
	BALIC_EMOTION,
	BALIC_MATERIAL,
	RAWHIDE,
	SOULSTONE,
	GATHERABLE,
	GATHERABLE_BONUS,
	DROP_MATERIAL,
	COINS,
	MEDALS,
	QUEST,
	KEY,
	CRAFT_BOOST,
	TAMPERING,
	COMBINATION,
	SKILLBOOK,
	GODSTONE,
	STIGMA_SHARD;

	private final long validEquipmentSlots;
	private final ItemSubType itemSubType;
	private final ArmorType armorType;
	private final boolean requiresMastery;

	private ItemGroup() {
		this(0, ItemSubType.NONE, false);
	}

	private ItemGroup(long validEquipmentSlots, ArmorType armorType) {
		this(validEquipmentSlots, armorType, false);
	}

	private ItemGroup(long validEquipmentSlots, ItemSubType itemSubType) {
		this(validEquipmentSlots, itemSubType, false);
	}

	private ItemGroup(long validEquipmentSlots, ItemSubType itemSubType, boolean requiresMastery) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = itemSubType;
		this.armorType = null;
		this.requiresMastery = requiresMastery;
	}

	private ItemGroup(long validEquipmentSlots, ArmorType armorType, boolean requiresMastery) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = ItemSubType.NONE;
		this.armorType = armorType;
		this.requiresMastery = requiresMastery;
	}

	public long getValidEquipmentSlots() {
		return validEquipmentSlots;
	}

	public ItemSubType getItemSubType() {
		return itemSubType;
	}

	public ArmorType getArmorType() {
		return armorType;
	}

	/**
	 * @return true if wearing items of this group needs a mastery skill (the skills themselves come from the skill data)
	 */
	public boolean requiresMastery() {
		return requiresMastery;
	}

	public EquipType getEquipType() {
		if (armorType != null)
			return EquipType.ARMOR;
		else
			return itemSubType.getEquipType();
	}

}
