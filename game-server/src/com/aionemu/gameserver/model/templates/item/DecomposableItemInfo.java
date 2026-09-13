package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author antness
 */
@XmlType(name = "DecomposableItem")
public class DecomposableItemInfo {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "selectable")
	private boolean isSelectable;
	@XmlAttribute(name = "only_one")
	private boolean isOnlyOne;
	@XmlElement(name = "set")
	private List<DecomposableSet> sets;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (sets == null)
			sets = Collections.emptyList();
		else if (isOnlyOne) // the likeliest branch gets the first roll, so the rare ones are only reached when it misses
			sets.sort(Comparator.comparing(DecomposableSet::getChance).reversed());
	}

	public int getItemId() {
		return itemId;
	}

	public boolean isSelectable() {
		return isSelectable;
	}

	public List<DecomposableSet> getSets() {
		return sets;
	}

	public DecomposableSet getSelectableSet(Player player) {
		for (DecomposableSet set : sets) {
			if (set.isApplicableTo(player) && !set.getItems().isEmpty())
				return set;
		}
		return null;
	}

	/**
	 * Rolls every applicable branch. Unless the item only ever yields one reward, every branch that fires contributes a reward.
	 */
	public List<DecomposedItem> decompose(Player player) {
		List<DecomposedItem> rewards = new ArrayList<>();
		for (DecomposableSet set : sets) {
			if (!set.isApplicableTo(player) || !set.roll())
				continue;
			DecomposedItem item = set.selectItem();
			if (item != null)
				rewards.add(item);
			if (isOnlyOne)
				break;
		}
		return rewards;
	}
}
