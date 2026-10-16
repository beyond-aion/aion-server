package com.aionemu.gameserver.model.templates.globaldrops;

import jakarta.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum StringFunction {
	START_WITH,
	END_WITH,
	CONTAINS,
	EQUALS;

}
