package com.aionemu.gameserver.model.templates.bounty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bounty")
public class BountyTemplate {

	@XmlAttribute(name = "item_id", required = true)
	private int itemId;
	@XmlAttribute(name = "count")
	private int count;
	
	public int getItemId() {
		return itemId;
	}
	
	public int getCount() {
		return count;
	}
}
