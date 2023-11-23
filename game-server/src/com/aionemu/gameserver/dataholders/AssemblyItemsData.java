package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.item.AssemblyItem;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assembly_items")
public class AssemblyItemsData {

	@XmlElement(name = "item", required = true)
	protected List<AssemblyItem> items;

	public int size() {
		return items.size();
	}

	public AssemblyItem getAssemblyItem(int itemId) {
		for (AssemblyItem assemblyItem : items) {
			if (assemblyItem.getId() == itemId) {
				return assemblyItem;
			}
		}
		return null;
	}
}
