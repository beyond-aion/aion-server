package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Rolandas
 */
@XmlEnum
public enum ItemActivationTarget {
	NONE,
	STANDALONE,
	TARGET,
	MYMENTO;

	public static ItemActivationTarget getFromString(String activationTarget) {
		for (ItemActivationTarget a : values()) {
			if (a.toString().equals(activationTarget))
				return a;
		}
		return NONE;
	}
}
