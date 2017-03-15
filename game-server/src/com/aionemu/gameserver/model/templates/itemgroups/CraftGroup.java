package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.Range;

import com.aionemu.gameserver.model.templates.rewards.CraftReward;

/**
 * @author Rolandas
 */
public abstract class CraftGroup extends BonusItemGroup {

	@XmlTransient
	private Map<Integer, Map<Range<Integer>, List<CraftReward>>> dataHolder;

	public ItemRaceEntry[] getRewards(Integer skillId) {
		if (!dataHolder.containsKey(skillId))
			return new ItemRaceEntry[0];
		List<ItemRaceEntry> result = new ArrayList<>();
		for (List<CraftReward> items : dataHolder.get(skillId).values())
			result.addAll(items);
		return result.toArray(new ItemRaceEntry[0]);
	}

	public ItemRaceEntry[] getRewards(Integer skillId, Integer skillPoints) {
		if (!dataHolder.containsKey(skillId))
			return new ItemRaceEntry[0];
		List<ItemRaceEntry> result = new ArrayList<>();
		for (Entry<Range<Integer>, List<CraftReward>> entry : dataHolder.get(skillId).entrySet()) {
			if (!entry.getKey().contains(skillPoints))
				continue;
			result.addAll(entry.getValue());
		}
		return result.toArray(new ItemRaceEntry[0]);
	}

	/**
	 * @return the dataHolder
	 */
	public Map<Integer, Map<Range<Integer>, List<CraftReward>>> getDataHolder() {
		return dataHolder;
	}

	/**
	 * @param craftShopBySkill
	 *          the dataHolder to set
	 */
	public void setDataHolder(Map<Integer, Map<Range<Integer>, List<CraftReward>>> craftShopBySkill) {
		this.dataHolder = craftShopBySkill;
	}
}
