package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "BuildingType")
@XmlEnum
public enum BuildingType {
	PERSONAL_FIELD(2),
	PERSONAL_INS(1);

	private int id;

	BuildingType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
