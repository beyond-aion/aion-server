package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlType(name = "item_type")
@XmlEnum
public enum ItemType {
	NORMAL,
	ABYSS,
	DRACONIC,
	DEVANION,
	LEGEND
}
