package com.aionemu.gameserver.model.templates.itemset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
