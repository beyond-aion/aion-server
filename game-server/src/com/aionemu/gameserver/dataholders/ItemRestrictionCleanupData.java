package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.restriction.ItemCleanupTemplate;

/**
 * @author KID
 */
@XmlRootElement(name = "item_restriction_cleanups")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemRestrictionCleanupData {
	@XmlElement(name = "cleanup")
	private List<ItemCleanupTemplate> bplist;

	public int size() {
		return bplist.size();
	}
	
	public List<ItemCleanupTemplate> getList() {
		return this.bplist;
	}
}
