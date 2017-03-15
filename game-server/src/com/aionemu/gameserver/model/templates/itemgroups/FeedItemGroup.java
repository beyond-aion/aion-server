package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeedItemGroup")
public abstract class FeedItemGroup {

	@XmlAttribute(name = "group", required = true)
	protected ItemGroupIndex index = ItemGroupIndex.NONE;

	@XmlElement(name = "item")
	private List<ItemRaceEntry> items;

	/**
	 * @return the index
	 */
	public ItemGroupIndex getIndex() {
		return index;
	}

	public List<ItemRaceEntry> getItems() {
		if (items == null) {
			items = new ArrayList<>();
		}
		return this.items;
	}

}
