package com.aionemu.gameserver.model.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
@XmlRootElement(name = "drop_group")
@XmlAccessorType(XmlAccessType.FIELD)
public class DropGroup implements DropCalculator {

	@XmlElement(name = "drop")
	protected List<Drop> drop;
	@XmlAttribute(name = "race")
	protected Race race = Race.PC_ALL;
	@XmlAttribute(name = "use_category")
	protected Boolean useCategory = true;
	@XmlAttribute(name = "name")
	protected String group_name;
	@XmlAttribute(name = "max_items")
	protected int maxItems = 1;

	/**
	 * @param drop
	 * @param race
	 * @param useCategory
	 * @param group_name
	 */
	public DropGroup(List<Drop> drop, Race race, Boolean useCategory, String group_name, int maxItems) {
		this.drop = drop;
		this.race = race;
		this.useCategory = useCategory;
		this.group_name = group_name;
		this.maxItems = maxItems;
	}

	public DropGroup() {
	}

	public List<Drop> getDrop() {
		return this.drop;
	}

	public Race getRace() {
		return race;
	}

	public Boolean isUseCategory() {
		return useCategory;
	}

	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * @return the name
	 */
	public String getGroupName() {
		if (group_name == null)
			return "";
		return group_name;
	}

	@Override
	public int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers) {
		// if enabled all items listed into every category will be checked and then Chance is calculated:
		// if chance successful maxDropsFromCategory counter will be increased
		// default value if useCategory == true is 1
		// default value if useCategory == false is 99 (means no limits).
		// if maxDropsFromCategory>= maxItems (who define the max items dropped from every single category) the dropcalculation will stop.

		if (DropConfig.DROP_ENABLE_SUPPORT_NEW_DROP_CATEGORY_CALCULATION && DropConfig.DROP_ENABLE_SUPPORT_NEW_NPCDROPS_FILES) {
			int maxDropsFromCategory = 0;

			List<Drop> copy = new ArrayList<>(drop); // create shallow copy of drops to shuffle
			Collections.shuffle(copy); // List needs to be shuffled so drops with higher indexes are not more likely excluded due to max_drop_group

			for (int i = 0; i < copy.size(); i++) {
				Drop d = copy.get(i);
				int oldIndex = index;
				// if check chance is successful maxDropsFromCategory counter is increased (default value == 1)
				index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
				if (oldIndex != index)
					maxDropsFromCategory++;
				// if counter maxDropsFromCategory >= DropConfig.DROP_MAX_ITEMS_BY_SINGLE_CATEGORY then exit from loop.
				if (maxDropsFromCategory >= maxItems)
					break;
			}
			copy.clear();
		} else {
			if (useCategory) {
				Drop d = drop.get(Rnd.get(0, drop.size() - 1));
				return d.dropCalculator(result, index, dropModifier, race, groupMembers);
			} else {
				for (int i = 0; i < drop.size(); i++) {
					Drop d = drop.get(i);
					index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
				}
			}
		}
		return index;
	}
}
