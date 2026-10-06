package com.aionemu.gameserver.model.templates.portal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author AionChs Master, Schattenlilie
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalItem")
public class PortalItem {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "itemid")
	protected int itemid;
	@XmlAttribute(name = "quantity")
	protected int quantity;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the itemid
	 */
	public int getItemid() {
		return itemid;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}
}
