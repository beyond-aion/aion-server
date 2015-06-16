package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingPassiveItem")
public class HousingPassiveItem extends PlaceableHouseObject {

	@Override
	public byte getTypeId() {
		return 0;
	}

}
