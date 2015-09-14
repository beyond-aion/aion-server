package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropItems")
public class GlobalDropItems {

	@XmlElement(name = "gd_item")
	protected List<GlobalDropItem> gdItems;

	public List<GlobalDropItem> getGlobalDropItems() {
		if (gdItems == null) {
			gdItems = new ArrayList<GlobalDropItem>();
		}
		return this.gdItems;
	}

	public void addItems(List<GlobalDropItem> value) {
		this.gdItems = value;
	}
}
