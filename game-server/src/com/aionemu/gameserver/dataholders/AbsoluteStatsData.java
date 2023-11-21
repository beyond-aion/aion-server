package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.stats.AbsoluteStatsTemplate;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "absoluteStats" })
@XmlRootElement(name = "absolute_stats")
public class AbsoluteStatsData {

	@XmlElement(name = "stats_set", required = true)
	protected List<AbsoluteStatsTemplate> absoluteStats;

	@XmlTransient
	private final Map<Integer, ModifiersTemplate> absoluteStatsData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (AbsoluteStatsTemplate stats : absoluteStats) {
			absoluteStatsData.put(stats.getId(), stats.getModifiers());
		}
		absoluteStats = null;
	}

	public ModifiersTemplate getTemplate(int statSetId) {
		return absoluteStatsData.get(statSetId);
	}

	public int size() {
		return absoluteStatsData.size();
	}

}
