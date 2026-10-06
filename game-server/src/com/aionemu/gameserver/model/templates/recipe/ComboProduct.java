package com.aionemu.gameserver.model.templates.recipe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComboProduct")
public class ComboProduct {

	@XmlAttribute
	protected int itemid;

	/**
	 * @return the itemid
	 */
	public int getItemId() {
		return itemid;
	}

}
