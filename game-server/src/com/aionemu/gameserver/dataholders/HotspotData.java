package com.aionemu.gameserver.dataholders;


import com.aionemu.gameserver.model.templates.hotspot.HotspotTemplate;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;

/**
 * @author ginho1
 */
@XmlRootElement(name = "hotspot_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class HotspotData {

	@XmlElement(name = "hotspot_location")
	private List<HotspotTemplate> hotspotTemplates;

	public int size() {
		if (hotspotTemplates == null) {
			hotspotTemplates = new ArrayList<HotspotTemplate>();
			return 0;
		}
		return hotspotTemplates.size();
	}

	public List<HotspotTemplate> getHotspotTemplates() {
		if (hotspotTemplates == null) {
			return new ArrayList<HotspotTemplate>();
		}
		return hotspotTemplates;
	}

	public HotspotTemplate getHotspotTemplateById(int id) {
		for (HotspotTemplate t : hotspotTemplates) {
			if(t.getId() == id)
				return t;
		}
		return null;
	}
}