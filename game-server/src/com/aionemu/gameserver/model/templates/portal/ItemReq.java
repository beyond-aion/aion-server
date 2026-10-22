package com.aionemu.gameserver.model.templates.portal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemReq")
public class ItemReq {

	@XmlAttribute(name = "item_id")
	protected int itemId;
	@XmlAttribute(name = "item_count")
	protected int itemCount;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int value) {
		this.itemId = value;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int value) {
		this.itemCount = value;
	}

}
