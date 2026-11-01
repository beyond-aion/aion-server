package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Where the bonus attributes of an item apply: only while it is equipped, or already while it is carried in the inventory.
 */
@XmlType(name = "bonusApply")
@XmlEnum
public enum BonusApply {

	@XmlEnumValue("equip")
	EQUIP,
	@XmlEnumValue("inventory")
	INVENTORY
}
