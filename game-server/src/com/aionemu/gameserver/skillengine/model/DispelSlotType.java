package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Neon
 */
@XmlType(name = "DispelSlotType")
@XmlEnum
public enum DispelSlotType {

	BUFF,
	DEBUFF,
	SPECIAL2;

}
