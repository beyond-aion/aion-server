package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
