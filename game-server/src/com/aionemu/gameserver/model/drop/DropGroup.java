package com.aionemu.gameserver.model.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
@XmlRootElement(name = "drop_group")
@XmlAccessorType(XmlAccessType.FIELD)
public class DropGroup {

	@XmlElement(name = "drop")
	private List<Drop> drops;
	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "level_based_chance_reduction")
	private boolean useLevelBasedChanceReduction;
	@XmlAttribute(name = "max_items")
	private int maxItems = 1;

	public List<Drop> getDrop() {
		return drops;
	}

	public Race getRace() {
		return race;
	}

	public int getMaxItems() {
		return maxItems;
	}

	public String getName() {
		return name;
	}

	public boolean isUseLevelBasedChanceReduction() {
		return useLevelBasedChanceReduction;
	}

	public int tryAddDropItems(Set<DropItem> result, int index, DropModifiers dropModifiers, Collection<Player> groupMembers) {
		Set<Drop> remainingDrops = new HashSet<>(drops);
		for (int i = 0; i < maxItems && !remainingDrops.isEmpty(); i++) {
			float chance = Rnd.chance();
			float nearestChanceDiff = Float.MAX_VALUE;
			List<Drop> nearestDropsOfSameChance = new ArrayList<>();
			for (Drop drop : remainingDrops) {
				float finalChance = dropModifiers.calculateDropChance(drop.getChance(), isUseLevelBasedChanceReduction());
				if (chance < finalChance) {
					float chanceDiff = finalChance - chance;
					if (nearestDropsOfSameChance.isEmpty() || chanceDiff <= nearestChanceDiff) {
						if (chanceDiff < nearestChanceDiff) {
							nearestDropsOfSameChance.clear();
							nearestChanceDiff = chanceDiff;
						}
						nearestDropsOfSameChance.add(drop);
					}
				}
			}
			Drop drop = Rnd.get(nearestDropsOfSameChance);
			if (drop != null) {
				index = addDropItem(index, result, drop, groupMembers);
				remainingDrops.remove(drop);
			}
		}
		return index;
	}

	private int addDropItem(int index, Set<DropItem> result, Drop drop, Collection<Player> groupMembers) {
		if (drop.isEachMember() && groupMembers != null && !groupMembers.isEmpty()) {
			for (Player player : groupMembers) {
				DropItem dropitem = new DropItem(drop);
				dropitem.calculateCount();
				dropitem.setIndex(index++);
				dropitem.setPlayerObjId(player.getObjectId());
				dropitem.setWinningPlayer(player);
				dropitem.isDistributeItem(true);
				result.add(dropitem);
			}
		} else {
			DropItem dropitem = new DropItem(drop);
			dropitem.calculateCount();
			dropitem.setIndex(index++);
			result.add(dropitem);
		}
		return index;
	}
}
