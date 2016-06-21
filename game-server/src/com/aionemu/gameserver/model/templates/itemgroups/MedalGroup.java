package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.templates.rewards.FullRewardItem;

/**
 * @author Luzien
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedalGroup")
public class MedalGroup extends BonusItemGroup {

	@XmlElement(name = "item")
	protected List<FullRewardItem> items;

	public List<FullRewardItem> getItems() {
		if (items == null) {
			items = new FastTable<FullRewardItem>();
		}
		return this.items;
	}

	@Override
	public ItemRaceEntry[] getRewards() {
		return getItems().toArray(new ItemRaceEntry[0]);
	}

}
