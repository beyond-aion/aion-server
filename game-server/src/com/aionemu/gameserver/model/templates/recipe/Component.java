package com.aionemu.gameserver.model.templates.recipe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Component")
public class Component {

	@XmlAttribute
	protected int itemid;
	@XmlAttribute
	protected int quantity;

	/**
	 * Gets the value of the itemid property.
	 */
	public Integer getItemid() {
		return itemid;
	}

	/**
	 * Gets the value of the quantity property.
	 */
	public Integer getQuantity() {
		return quantity;
	}
}
