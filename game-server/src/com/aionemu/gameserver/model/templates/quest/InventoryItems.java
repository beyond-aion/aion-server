package com.aionemu.gameserver.model.templates.quest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InventoryItems", propOrder = { "inventoryItem" })
public class InventoryItems {

	@XmlElement(name = "inventory_item")
	protected List<InventoryItem> inventoryItem;

	/**
	 * Gets the value of the inventoryItem property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the collectItem property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInventoryItem().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link InventoryItem }
	 */
	public List<InventoryItem> getInventoryItem() {
		if (inventoryItem == null) {
			inventoryItem = new FastTable<InventoryItem>();
		}
		return this.inventoryItem;
	}

}
