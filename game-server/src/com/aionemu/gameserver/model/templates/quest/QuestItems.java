package com.aionemu.gameserver.model.templates.quest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestItems")
public class QuestItems {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute
	private long count = 1L;

	/**
	 * Constructor used by unmarshaller
	 */
	public QuestItems() {
		this.count = 1L;
	}

	public QuestItems(int itemId, long count) {
		this.itemId = itemId;
		this.count = count;
	}

	/**
	 * Gets the value of the itemId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Gets the value of the count property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public long getCount() {
		return count;
	}

}
