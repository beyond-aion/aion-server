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

	/**
	 * Constructors
	 */
	private AttendType(int id) {
		this.id = id;
	}

	/**
	 * Accessors
	 */
	public int getId() {
		return id;
	}
}
