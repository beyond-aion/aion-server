package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingNpc")
public class HousingNpc extends PlaceableHouseObject {

	@XmlAttribute(name = "npc_id", required = true)
	protected int npcId;

	/**
	 * Gets the value of the npcId property.
	 */
	public int getNpcId() {
		return npcId;
	}

	@Override
	public byte getTypeId() {
		return 7;
	}

}
