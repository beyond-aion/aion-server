package com.aionemu.gameserver.dataholders;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;

/**
 * @author Yeats
 *
 */
@XmlRootElement(name="legion_dominion_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegionDominionData {
	
	@XmlElement(name = "legion_dominion_location")
	private List<LegionDominionLocationTemplate> ldl;
	
	private Map<Integer, LegionDominionLocation> locations = new TreeMap<>();
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		locations.clear();
		for (LegionDominionLocationTemplate temp : ldl) {
			LegionDominionLocation loc = new LegionDominionLocation(temp);
			locations.put(temp.getId(), loc);
		}
	}

	public int size() {
		return locations.size();
	}

	public Map<Integer, LegionDominionLocation> getLegionDominionLocations() {
		return locations;
	}
}
