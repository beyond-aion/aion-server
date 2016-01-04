package com.aionemu.gameserver.model.templates.item.enums;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author xTz
 */
@XmlEnum
public enum ItemSubType {

	ALL_ARMOR(ArmorType.GENERAL),
	NONE(EquipType.NONE),
	CHAIN(ArmorType.GENERAL),
	CLOTHES(ArmorType.GENERAL),
	LEATHER(ArmorType.GENERAL),
	PLATE(ArmorType.GENERAL),
	ROBE(ArmorType.GENERAL),
	SHIELD(ArmorType.GENERAL),
	ARROW(EquipType.NONE),
	WING(ArmorType.GENERAL),
	ONE_HAND(EquipType.WEAPON),
	TWO_HAND(EquipType.WEAPON),
	STIGMA(EquipType.STIGMA),
	PLUME(EquipType.PLUME);

	private final ArmorType armorType;
	private final EquipType equipType;

	private ItemSubType(ArmorType armorType) {
		this.armorType = armorType;
		this.equipType = EquipType.ARMOR;
	}

	private ItemSubType(EquipType equipType) {
		this.equipType = equipType;
		this.armorType = null;
	}

	public ArmorType getArmorType() {
		return armorType;
	}

	protected EquipType getEquipType() {
		return equipType;
	}

}
