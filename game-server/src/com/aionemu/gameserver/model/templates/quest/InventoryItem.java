package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InventoryItem")
public class InventoryItem {

	@XmlAttribute(name = "item_id")
	protected Integer itemId;

	@XmlAttribute
	private Integer count;

	/**
	 * Gets the value of the itemId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * @return the count, or null if scripts should handle that the counts probably may depend on scenario
	 */
	public Integer getCount() {
		return count;
	}

}
