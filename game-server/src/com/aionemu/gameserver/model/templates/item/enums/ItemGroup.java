package com.aionemu.gameserver.model.templates.item.enums;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.items.ItemSlot;

/**
 * @author xTz
 */
@XmlType(name = "item_group")
@XmlEnum
public enum ItemGroup {
	NONE,
	NOWEAPON(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	SWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND),
	GREATSWORD(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	EXTRACT_SWORD(0, ItemSubType.NONE),
	DAGGER(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND),
	MACE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND),
	ORB(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	SPELLBOOK(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	POLEARM(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	STAFF(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	BOW(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	HARP(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	GUN(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.ONE_HAND),
	CANNON(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	KEYBLADE(ItemSlot.MAIN_OR_SUB.getSlotIdMask(), ItemSubType.TWO_HAND),
	SHIELD(ItemSlot.SUB_HAND.getSlotIdMask(), ItemSubType.SHIELD),

	TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ALL_ARMOR),
	GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ALL_ARMOR),
	SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ALL_ARMOR),
	PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ALL_ARMOR),
	SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ALL_ARMOR),
	RB_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.ROBE),
	RB_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.ROBE),
	RB_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.ROBE),
	RB_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.ROBE),
	RB_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.ROBE),
	CL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CLOTHES),
	CL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CLOTHES),
	LT_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.LEATHER),
	LT_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.LEATHER),
	LT_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.LEATHER),
	LT_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.LEATHER),
	LT_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.LEATHER),
	CH_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.CHAIN),
	CH_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.CHAIN),
	CH_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.CHAIN),
	CH_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.CHAIN),
	CH_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.CHAIN),
	PL_TORSO(ItemSlot.TORSO.getSlotIdMask(), ItemSubType.PLATE),
	PL_GLOVE(ItemSlot.GLOVES.getSlotIdMask(), ItemSubType.PLATE),
	PL_SHOULDER(ItemSlot.SHOULDER.getSlotIdMask(), ItemSubType.PLATE),
	PL_PANTS(ItemSlot.PANTS.getSlotIdMask(), ItemSubType.PLATE),
	PL_SHOES(ItemSlot.BOOTS.getSlotIdMask(), ItemSubType.PLATE),

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
	private final ArmorType armorCategory;

	ItemGroup() {
		this(0, ItemSubType.NONE);
	}

	ItemGroup(long validEquipmentSlots, ItemSubType itemSubType) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = itemSubType;
		this.armorCategory = null;
	}

	ItemGroup(long validEquipmentSlots, ArmorType armorCategory) {
		this.validEquipmentSlots = validEquipmentSlots;
		this.itemSubType = ItemSubType.NONE;
		this.armorCategory = armorCategory;
	}

	public long getValidEquipmentSlots() {
		return validEquipmentSlots;
	}

	public ItemSubType getItemSubType() {
		return itemSubType;
	}

	public ArmorType getArmorCategory() {
		return armorCategory;
	}

	public EquipType getEquipType() {
		if (armorCategory != null)
			return EquipType.ARMOR;
		else
			return itemSubType.getEquipType();
	}

}
