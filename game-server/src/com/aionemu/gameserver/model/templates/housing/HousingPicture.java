package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingPicture")
public class HousingPicture extends PlaceableHouseObject {

	@Override
	public byte getTypeId() {
		// TODO Not sniffed yet
		return 0;
	}

}
