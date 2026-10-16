package com.aionemu.gameserver.model.templates.itemset;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlRootElement(name = "ItemPart")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPart {

	@XmlAttribute
	protected int itemid;

	/**
	 * @return the itemid
	 */
	public int getItemId() {
		return itemid;
	}
}
