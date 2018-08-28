package com.aionemu.gameserver.model.templates.siegelocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
