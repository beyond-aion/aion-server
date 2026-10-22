package com.aionemu.gameserver.model.templates.housing;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingJukeBox")
public class HousingJukeBox extends PlaceableHouseObject {

	@Override
	public byte getTypeId() {
		// TODO Not sniffed yet
		return 6;
	}

}
