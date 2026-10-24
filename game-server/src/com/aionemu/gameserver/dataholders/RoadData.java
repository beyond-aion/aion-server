package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.road.RoadTemplate;

/**
 * @author SheppeR
 */
@XmlRootElement(name = "roads")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoadData {

	@XmlElement(name = "road")
	private List<RoadTemplate> roadTemplates;

	public int size() {
		return getRoadTemplates().size();
	}

	public List<RoadTemplate> getRoadTemplates() {
		return roadTemplates == null ? List.of() : roadTemplates;
	}
}
