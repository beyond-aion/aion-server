package com.aionemu.gameserver.model.templates.world;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Element a weather is made of, kept for NPC AI scripts which read it through the weather query. Nothing in the
 * emulator consumes it yet.
 */
@XmlType(name = "WeatherAttribute")
@XmlEnum
public enum WeatherAttribute {

	@XmlEnumValue("water")
	WATER,
	@XmlEnumValue("air")
	AIR,
	@XmlEnumValue("earth")
	EARTH,
	@XmlEnumValue("fire")
	FIRE
}
