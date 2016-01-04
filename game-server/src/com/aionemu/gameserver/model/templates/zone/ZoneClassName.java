package com.aionemu.gameserver.model.templates.zone;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlType(name = "ZoneClassName")
@XmlEnum
public enum ZoneClassName {
	DUMMY,
	SUB,
	FLY,
	NO_FLY,
	ARTIFACT,
	FORT,
	LIMIT,
	ITEM_USE,
	PVP,
	DUEL,
	HOUSE,
	WEATHER,
	DOMINION;
}
