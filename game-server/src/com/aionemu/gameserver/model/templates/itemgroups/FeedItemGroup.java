package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.List;

import jakarta.xml.bind.annotation.*;

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

	public ItemGroupIndex getIndex() {
		return index;
	}

	public List<ItemRaceEntry> getItems() {
		return items == null ? List.of() : items;
	}
}
