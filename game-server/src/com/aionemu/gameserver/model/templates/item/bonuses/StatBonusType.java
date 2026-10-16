package com.aionemu.gameserver.model.templates.item.bonuses;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "StatBonusType")
@XmlEnum
public enum StatBonusType {

	INVENTORY,
	POLISH
}
