package com.aionemu.gameserver.model.templates.chest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyItem")
public class KeyItem {

	@XmlAttribute(name = "item_ids")
	private List<Integer> itemIds;
	@XmlAttribute(name = "count")
	private int count;

	public List<Integer> getItemIds() {
		return itemIds;
	}

	public int getCount() {
		return count;
	}
}
