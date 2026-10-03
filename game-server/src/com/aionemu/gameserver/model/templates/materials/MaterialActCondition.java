package com.aionemu.gameserver.model.templates.materials;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType
@XmlEnum
public enum MaterialActCondition {

	SUNNY,
	NIGHT;
}
