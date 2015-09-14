package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.globaldrops.GlobalExclusion;

/**
 * @author bobobear
 */
@XmlRootElement(name = "global_npc_exclusions")
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalNpcExclusionData {

	@XmlElement(name = "global_exclusion", type = GlobalExclusion.class)
	protected List<GlobalExclusion> list;

	// @XmlTransient
	private final List<GlobalExclusion> globalExclusionsData = new ArrayList<GlobalExclusion>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GlobalExclusion template : list) {
			globalExclusionsData.add(template);
		}
		list.clear();
	}

	public int size() {
		return globalExclusionsData.size();
	}

	public List<GlobalExclusion> getGlobalExclusions() {
		return globalExclusionsData;
	}

}
