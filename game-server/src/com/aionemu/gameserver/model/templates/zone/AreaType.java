package com.aionemu.gameserver.model.templates.zone;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlType(name = "AreaType")
@XmlEnum
public enum AreaType {
	POLYGON,
	CYLINDER,
	SPHERE,
	SEMISPHERE;
}
