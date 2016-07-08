package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestItems")
public class QuestItems {

	@XmlAttribute(name = "item_id")
	protected Integer itemId;
	@XmlAttribute
	protected long count = 1L;

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
	public Integer getItemId() {
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
