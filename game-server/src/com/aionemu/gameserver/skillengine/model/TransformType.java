package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author kecimis
 */

@XmlType(name = "TransformType")
@XmlEnum
public enum TransformType {
	NONE(0),
	PC(1),
	AVATAR(2),
	FORM1(3);

	private int id;

	private TransformType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
