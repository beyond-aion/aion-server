package com.aionemu.gameserver.model.templates.item.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlType(name = "item_group")
@XmlEnum
public enum ItemGroup {
	NONE,
	NOWEAPON(3, ItemSubType.TWO_HAND),
	SWORD(3, ItemSubType.ONE_HAND, new int[] { 37, 44 }),
	GREATSWORD(3, ItemSubType.TWO_HAND, new int[] { 51 }),
	EXTRACT_SWORD(0, ItemSubType.NONE),
	DAGGER(3, ItemSubType.ONE_HAND, new int[] { 66, 45 }),
	MACE(3, ItemSubType.ONE_HAND, new int[] { 39, 46 }),
	ORB(3, ItemSubType.TWO_HAND, new int[] { 111 }),
	SPELLBOOK(3, ItemSubType.TWO_HAND, new int[] { 100 }),
	POLEARM(3, ItemSubType.TWO_HAND, new int[] { 52 }),
	STAFF(3, ItemSubType.TWO_HAND, new int[] { 89 }),
	BOW(3, ItemSubType.TWO_HAND, new int[] { 53 }),
	HARP(3, ItemSubType.TWO_HAND, new int[] { 124, 114 }),
	GUN(3, ItemSubType.ONE_HAND, new int[] { 117, 112 }),
	CANNON(3, ItemSubType.TWO_HAND, new int[] { 113 }),
	KEYBLADE(3, ItemSubType.TWO_HAND, new int[] { 112, 115 }),
	SHIELD(1 << 1, ItemSubType.SHIELD, new int[] { 43, 50 }),
	
	TORSO(1 << 3, ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	GLOVE(1 << 4, ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	SHOULDER(1 << 11, ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	PANTS(1 << 12, ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	SHOES(1 << 5, ItemSubType.ALL_ARMOR, new int[] { 103, 106 }),
	RB_TORSO(1 << 3, ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_GLOVE(1 << 4, ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_SHOULDER(1 << 11, ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_PANTS(1 << 12, ItemSubType.ROBE, new int[] { 103, 106 }),
	RB_SHOES(1 << 5, ItemSubType.ROBE, new int[] { 103, 106 }),
	CL_TORSO(1 << 3, ItemSubType.CLOTHES, new int[] { 40 }),
	CL_GLOVE(1 << 4, ItemSubType.CLOTHES, new int[] { 40 }),
	CL_SHOULDER(1 << 11, ItemSubType.CLOTHES, new int[] { 40 }),
	CL_PANTS(1 << 12, ItemSubType.CLOTHES, new int[] { 40 }),
	CL_SHOES(1 << 5, ItemSubType.CLOTHES, new int[] { 40 }),
	LT_TORSO(1 << 3, ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_GLOVE(1 << 4, ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_SHOULDER(1 << 11, ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_PANTS(1 << 12, ItemSubType.LEATHER, new int[] { 41, 48 }),
	LT_SHOES(1 << 5, ItemSubType.LEATHER, new int[] { 41, 48 }),
	CH_TORSO(1 << 3, ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_GLOVE(1 << 4, ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_SHOULDER(1 << 11, ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_PANTS(1 << 12, ItemSubType.CHAIN, new int[] { 42, 49 }),
	CH_SHOES(1 << 5, ItemSubType.CHAIN, new int[] { 42, 49 }),
	PL_TORSO(1 << 3, ItemSubType.PLATE, new int[] { 54 }),
	PL_GLOVE(1 << 4, ItemSubType.PLATE, new int[] { 54 }),
	PL_SHOULDER(1 << 11, ItemSubType.PLATE, new int[] { 54 }),
	PL_PANTS(1 << 12, ItemSubType.PLATE, new int[] { 54 }),
	PL_SHOES(1 << 5, ItemSubType.PLATE, new int[] { 54 }),
	
	EARRING(192, ArmorType.ACCESSORY),
	RING(768, ArmorType.ACCESSORY),
	NECKLACE(1 << 10, ArmorType.ACCESSORY),
	BELT(1 << 16, ArmorType.ACCESSORY),
	WING(1 << 15, ItemSubType.WING),
	PLUME(1 << 19, ItemSubType.PLUME),

	HEAD(1 << 2, ArmorType.ACCESSORY),
	LT_HEADS(1 << 2, ItemSubType.LEATHER),
	CL_HEADS(1 << 2, ItemSubType.CLOTHES),
	CL_MULTISLOT(10, ItemSubType.CLOTHES),
	CL_SHIELD(1 << 1, ArmorType.ACCESSORY),
	
	POWER_SHARDS(24576, ArmorType.ACCESSORY),
	TGFSL_ROBE(10, ItemSubType.ROBE),
	STIGMA((long) 7E003F << 30, ItemSubType.STIGMA),
	// other
	ARROW(0, ItemSubType.ARROW),
	NPC_MACE(1, ItemSubType.ONE_HAND), // keep it above TOOLHOES, for search picking it up
	TOOLRODS(3, ItemSubType.TWO_HAND),
	TOOLHOES(1, ItemSubType.ONE_HAND),
	TOOLPICKS(3, ItemSubType.TWO_HAND),
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

	private final long slot;
	private final ItemSubType itemSubType;
	private final ArmorType armorType;
	private final int[] requiredSkill;

	private ItemGroup() {
		this(0, ItemSubType.NONE, new int[] {});
	}

	private ItemGroup(long slot, ArmorType armorType) {
		this(slot, armorType, new int[] {});
	}

	private ItemGroup(long slot, ItemSubType itemSubType) {
		this(slot, itemSubType, new int[] {});
	}

	private ItemGroup(long slot, ItemSubType itemSubType, int[] requiredSkill) {
		this.slot = slot;
		this.itemSubType = itemSubType;
		this.armorType = null;
		this.requiredSkill = requiredSkill;
	}

	private ItemGroup(long slot, ArmorType armorType, int[] requiredSkill) {
		this.slot = slot;
		this.itemSubType = ItemSubType.NONE;
		this.armorType = armorType;
		this.requiredSkill = requiredSkill;
	}

	public long getSlots() {
		return slot;
	}

	public ItemSubType getItemSubType() {
		return itemSubType;
	}

	public ArmorType getArmorType() {
		return armorType;
	}

	public int[] getRequiredSkills(String subtypePrefix) {
		return requiredSkill;
	}

	public EquipType getEquipType() {
		if (armorType != null)
			return EquipType.ARMOR;
		else
			return itemSubType.getEquipType();
	}

}
