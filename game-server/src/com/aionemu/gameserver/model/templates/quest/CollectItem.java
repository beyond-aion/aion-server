package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItem")
public class CollectItem {

	@XmlAttribute(name = "item_id")
	protected Integer itemId;
	@XmlAttribute
	protected Integer count;

	/**
	 * Gets the value of the itemId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * Gets the value of the count property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getCount() {
		return count;
	}

}
