package com.aionemu.gameserver.model.templates.staticdoor;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "DoorType")
@XmlEnum
public enum DoorType {
	DOOR,
	ABYSS,
	HOUSE
}
