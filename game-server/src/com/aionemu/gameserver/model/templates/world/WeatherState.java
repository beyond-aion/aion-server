package com.aionemu.gameserver.model.templates.world;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * What a weather physically brings, as shown by its particle effect in the client.
 */
@XmlType(name = "WeatherState")
@XmlEnum
public enum WeatherState {
	RAIN,
	SNOW,
	WIND
}
