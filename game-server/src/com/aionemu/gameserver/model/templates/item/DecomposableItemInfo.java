package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "DecomposableItem")
public class DecomposableItemInfo {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "selectable")
	private boolean isSelectable = false;
	@XmlElement(name = "items")
	private List<ExtractedItemsCollection> itemsCollections;

	public int getItemId() {
		return itemId;
	}

	public boolean isIsSelectable() {
		return isSelectable;
	}

	public List<ExtractedItemsCollection> getItemsCollections() {
		return itemsCollections;
	}
}
