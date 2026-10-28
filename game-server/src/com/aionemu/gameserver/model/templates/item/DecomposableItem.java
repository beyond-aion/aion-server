package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author antness
 */
@XmlType(name = "DecomposableItem")
public class DecomposableItem {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "selectable")
	private boolean isSelectable;
	@XmlAttribute(name = "only_one")
	private boolean isOnlyOne;
	@XmlAttribute(name = "override")
	private boolean isOverride;
	@XmlElement(name = "set")
	private List<DecomposableSet> sets;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (sets == null)
			sets = Collections.emptyList();
		if (isSelectable && sets.stream().anyMatch(set -> set.getRewards().stream().anyMatch(reward -> reward instanceof DecomposedBundle)))
			throw new IllegalArgumentException("Selectable decomposable item " + itemId + " cannot offer bundles");
		// the selection window lists the first applicable set as a whole and grants the picked item, nothing is rolled
		if (isSelectable && sets.stream().anyMatch(set -> set.getChance() != 100 || set.getRewards().stream().anyMatch(reward -> reward.getChance() != 100)))
			throw new IllegalArgumentException("Selectable decomposable item " + itemId + " cannot have chances");
		if (!isSelectable && isOverride && sets.stream().anyMatch(set -> set.getRewards().stream().mapToDouble(DecomposedReward::getChance).sum() >= 100.1))
			throw new IllegalArgumentException("The sum of chances in a set must not exceed 100 % (decomposable item " + itemId + ")");
		if (isOnlyOne) // the rarest branch gets the first roll, so the likelier ones are only reached when the rarer ones miss
			sets.sort(Comparator.comparing(DecomposableSet::getChance));
	}

	public int getItemId() {
		return itemId;
	}

	public boolean isSelectable() {
		return isSelectable;
	}

	public boolean isOverride() {
		return isOverride;
	}

	public List<DecomposableSet> getSets() {
		return sets;
	}

	public DecomposableSet getSelectableSet(Player player) {
		for (DecomposableSet set : sets) {
			if (set.isApplicableTo(player) && !set.getRewards().isEmpty())
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
			DecomposedReward reward = set.selectReward();
			if (reward != null)
				rewards.addAll(reward.getItems());
			if (isOnlyOne)
				break;
		}
		return rewards;
	}
}
