package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "activationAttribute")
@XmlEnum
public enum ActivationAttribute {
	NONE,
	ACTIVE,
	PROVOKED,
	MAINTAIN,
	TOGGLE,
	PASSIVE,
	CHARGE
}
