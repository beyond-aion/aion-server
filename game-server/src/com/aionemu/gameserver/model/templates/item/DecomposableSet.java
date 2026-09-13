package com.aionemu.gameserver.model.templates.item;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * One reward branch of a decomposable item. Branches are rolled independently of each other, and a branch that fires yields exactly one of its
 * weighted alternatives (or nothing at all, if their chances don't add up to 100).
 */
@XmlType(name = "DecomposableSet")
public class DecomposableSet {

	@XmlAttribute(name = "chance")
	private float chance = 100;
	@XmlAttribute(name = "min_level")
	private int minLevel = 1;
	@XmlAttribute(name = "max_level")
	private int maxLevel = 80;
	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;
	@XmlList
	@XmlAttribute(name = "player_classes")
	private List<PlayerClass> playerClasses;
	@XmlElement(name = "item")
	private List<DecomposedItem> items;

	public float getChance() {
		return chance;
	}

	public List<DecomposedItem> getItems() {
		return items != null ? items : Collections.emptyList();
	}

	public boolean isApplicableTo(Player player) {
		return (race == Race.PC_ALL || race == player.getRace()) && (playerClasses == null || playerClasses.contains(player.getPlayerClass()))
			&& player.getLevel() >= minLevel && player.getLevel() <= maxLevel;
	}

	public boolean roll() {
		return Rnd.get(1, 1000) <= Math.round(chance * 10);
	}

	/**
	 * @return The one alternative this branch yields, or null if the alternatives don't cover the whole roll.
	 */
	public DecomposedItem selectItem() {
		int roll = Rnd.get(1, 10000);
		int sum = 0;
		for (DecomposedItem item : getItems()) {
			sum += Math.round(item.getChance() * 100);
			if (roll <= sum)
				return item;
		}
		return null;
	}
}
