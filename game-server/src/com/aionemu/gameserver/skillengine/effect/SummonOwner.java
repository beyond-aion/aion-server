package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * @author Rolandas
 */
@XmlEnum
public enum SummonOwner {
	PRIVATE,
	GROUP,
	LEGION,
	ALLIANCE
}
