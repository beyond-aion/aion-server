package com.aionemu.gameserver.skillengine.properties;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Cheatkiller
 */
@XmlType(name = "directions")
@XmlEnum
public enum AreaDirections {

	NONE,
	FRONT,
	BACK
}
