package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats, Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LegionDominionReward")
public class LegionDominionReward {

	@XmlAttribute(name = "rank")
	protected int rank;
	@XmlAttribute(name = "item_id")
	protected int itemId;
	@XmlAttribute(name = "count")
	protected int count;

	public int getRank() {
		return rank;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

}
