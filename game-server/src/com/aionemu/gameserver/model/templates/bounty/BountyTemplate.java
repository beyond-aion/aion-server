package com.aionemu.gameserver.model.templates.bounty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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
