package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Cheatkiller
 */
@XmlType(name = "StigmaType")
@XmlEnum
public enum StigmaType {

	NONE(0),
	BASIC(1),
	ADVANCED(2);

	private int id;

	private StigmaType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
