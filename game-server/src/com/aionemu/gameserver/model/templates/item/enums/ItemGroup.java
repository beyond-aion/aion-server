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
	SWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, new int[] { 37, 44 }),
	GREATSWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 51 }),
	EXTRACT_SWORD(0, ItemSubType.NONE),
	DAGGER(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, new int[] { 66, 45 }),
	MACE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, new int[] { 39, 46 }),
	ORB(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 111 }),
	SPELLBOOK(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 100 }),
	POLEARM(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 52 }),
	STAFF(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 89 }),
	BOW(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 53 }),
	HARP(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 124, 114 }),
	GUN(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND, new int[] { 117, 112 }),
	CANNON(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 113 }),
	KEYBLADE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND, new int[] { 112, 115 }),
	SHIELD(ItemSlot.SUB_HAND.getSlotIdMask(), ItemSubType.SHIELD, new int[] { 43, 50 }),
	
	TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	RB_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ROBE, new int[] { 103, 106 }),
	CL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CLOTHES, new int[] { 40 }),
	CL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CLOTHES, new int[] { 40 }),
	CL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CLOTHES, new int[] { 40 }),
	CL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CLOTHES, new int[] { 40 }),
	CL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CLOTHES, new int[] { 40 }),
	LT_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.LEATHER, new int[] { 41, 48 }),
	CH_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CHAIN, new int[] { 42, 49 }),
	PL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.PLATE, new int[] { 54 }),
	PL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.PLATE, new int[] { 54 }),
	PL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.PLATE, new int[] { 54 }),
	PL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.PLATE, new int[] { 54 }),
	PL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.PLATE, new int[] { 54 }),
	
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
	private final int[] requiredSkill;

	private ItemGroup() {
		this(0, ItemSubType.NONE, new int[] {});
	}

	private ItemGroup(long validEquipmentSlots, ArmorType armorType) {
		this(validEquipmentSlots, armorType, new int[] {});
	}

	private ItemGroup(long validEquipmentSlots, ItemSubType itemSubType) {
		this(validEquipmentSlots, itemSubType, new int[] {});
	}

	private ItemGroup(long validEquipmentSlots, ItemSubType itemSubType, int[] requiredSkill) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = itemSubType;
		this.armorType = null;
		this.requiredSkill = requiredSkill;
	}

	private ItemGroup(long validEquipmentSlots, ArmorType armorType, int[] requiredSkill) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = ItemSubType.NONE;
		this.armorType = armorType;
		this.requiredSkill = requiredSkill;
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

	public int[] getRequiredSkills() {
		return requiredSkill;
	}

	public EquipType getEquipType() {
		if (armorType != null)
			return EquipType.ARMOR;
		else
			return itemSubType.getEquipType();
	}

}
