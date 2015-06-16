package com.aionemu.gameserver.model.templates.siegelocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiegeReward")
public class SiegeReward {

	@XmlAttribute(name = "top")
	protected int top;
	@XmlAttribute(name = "itemid")
	protected int itemId;
	@XmlAttribute(name = "m_count")
	protected int mCount;

	public int getTop() {
		return top;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return mCount;
	}
}
