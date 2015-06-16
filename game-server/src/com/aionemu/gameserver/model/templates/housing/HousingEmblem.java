package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingEmblem")
public class HousingEmblem extends PlaceableHouseObject {

	@XmlAttribute(name = "level", required = true)
	private int level;

	@Override
	public byte getTypeId() {
		return 11;
	}

	public int getLevel() {
		return level;
	}

}
