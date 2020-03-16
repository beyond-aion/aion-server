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
	BRAXCAFE(1 << 3), // 8
	// 16, 32, 64, 128 are test IDs on map 900020000 Test_Basic
	TEST_BASIC_1(1 << 4), // 16
	TEST_BASIC_2(1 << 5), // 32
	TEST_BASIC_3(1 << 6), // 64
	TEST_BASIC_4(1 << 7); // 128

	private final int id;

	EventTheme(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
