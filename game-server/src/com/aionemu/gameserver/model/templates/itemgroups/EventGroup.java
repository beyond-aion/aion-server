package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.rewards.FullRewardItem;

/**
 * @author Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventGroup")
public class EventGroup extends BonusItemGroup {

	@XmlElement(name = "item")
	private List<FullRewardItem> items;

	@Override
	public List<FullRewardItem> getItems() {
		return items == null ? Collections.emptyList() : items;
	}

}
