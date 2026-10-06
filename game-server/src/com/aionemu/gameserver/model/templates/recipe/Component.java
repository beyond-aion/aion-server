package com.aionemu.gameserver.model.templates.recipe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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

	public int getItemId() {
		return itemid;
	}

	public int getQuantity() {
		return quantity;
	}
}
