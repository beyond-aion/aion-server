package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlType(name = "EventTheme")
@XmlEnum
public enum EventTheme {

	NONE(0),
	CHRISTMAS(1 << 0), // 1
	HALLOWEEN(1 << 1), // 2
	VALENTINE(1 << 2), // 4
	BRAXCAFE(1 << 3); // 8

	private final int id;

	private EventTheme(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
