package com.aionemu.gameserver.model.templates.item;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "ResultedItemsCollection")
public class ResultedItemsCollection {

	@XmlElement(name = "item")
	private List<ResultedItem> items;
	@XmlElement(name = "random_item")
	private List<RandomItem> randomItems;

	public List<ResultedItem> getItems() {
		return items != null ? items : Collections.emptyList();
	}

	public List<RandomItem> getRandomItems() {
		return randomItems != null ? randomItems : Collections.emptyList();
	}
}
