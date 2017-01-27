package com.aionemu.gameserver.model.drop;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
public class DropGroup implements DropCalculator {

	@XmlElement(name = "drop")
	protected List<Drop> drops;
	@XmlAttribute(name = "race")
	protected Race race = Race.PC_ALL;
	@XmlAttribute(name = "name")
	protected String groupName;
	@XmlAttribute(name = "max_items")
	protected int maxItems = 1;

	public DropGroup() {
	}

	public List<Drop> getDrop() {
		return drops;
	}

	public Race getRace() {
		return race;
	}

	public int getMaxItems() {
		return maxItems;
	}

	public String getGroupName() {
		return groupName;
	}

	@Override
	public int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers) {
		if (maxItems == 1) { // this block generates less overhead
			Drop d;
			if (drops.size() > 1) {
				List<Drop> safeDrops = drops.stream().filter(drop -> drop.getChance() >= 100).collect(Collectors.toList());
				if (!safeDrops.isEmpty())
					d = Rnd.get(safeDrops);
				else
					d = Rnd.get(drops);
			} else
				d = drops.get(0);
			index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
		} else if (maxItems > 1) {
			int iterationCount = 0;

			List<Drop> safeDrops = drops.stream().filter(drop -> drop.getChance() >= 100).collect(Collectors.toList());
			if (!safeDrops.isEmpty()) {
				Collections.shuffle(safeDrops);
				for (Drop d : safeDrops) {
					index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
					if (++iterationCount >= maxItems)
						return index;
				}
			}

			List<Drop> unsafeDrops = drops.stream().filter(drop -> drop.getChance() < 100).collect(Collectors.toList());
			Collections.shuffle(unsafeDrops);
			for (int i = 0; i < unsafeDrops.size(); i++) {
				Drop d = unsafeDrops.get(i);
				index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
				if (++iterationCount >= maxItems) // check every iteration to ensure not to always drop maxItem count if there are many items in a group
					return index;
			}
		}
		return index;
	}
}
