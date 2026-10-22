package com.aionemu.gameserver.model.templates.teleport;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "type")
@XmlEnum
public enum TeleportType {
	REGULAR,
	FLIGHT
}
