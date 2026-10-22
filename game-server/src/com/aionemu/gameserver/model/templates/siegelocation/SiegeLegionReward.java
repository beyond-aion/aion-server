package com.aionemu.gameserver.model.templates.siegelocation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiegeLegionReward")
public class SiegeLegionReward {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "item_count")
	private long itemCount;

	public int getItemId() {
		return itemId;
	}

	public long getItemCount() {
		return itemCount;
	}
}
