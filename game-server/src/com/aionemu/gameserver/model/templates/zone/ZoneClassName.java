package com.aionemu.gameserver.model.templates.zone;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlType(name = "ZoneClassName")
@XmlEnum
public enum ZoneClassName {
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
