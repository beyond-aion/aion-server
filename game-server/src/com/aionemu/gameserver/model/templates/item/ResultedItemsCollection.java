package com.aionemu.gameserver.model.templates.item;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

/**
 * @author antness
 */
@XmlType(name = "ResultedItemsCollection")
public class ResultedItemsCollection {

	@XmlElement(name = "item")
	protected List<ResultedItem> items;
	@XmlElement(name = "random_item")
	protected List<RandomItem> randomItems;

	public List<ResultedItem> getItems() {
		return items != null ? items : Collections.<ResultedItem> emptyList();
	}

	public List<RandomItem> getRandomItems() {
		if (randomItems != null) {
			return randomItems;
		} else {
			return new FastTable<RandomItem>();
		}
	}
}
