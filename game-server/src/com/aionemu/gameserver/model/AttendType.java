package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Alcapwnd
 */
@XmlEnum
public enum AttendType {

	DAILY(0),
	ANNIVERSARY(1),
	CUMULATIVE(2);

	private int id;

	AttendType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
