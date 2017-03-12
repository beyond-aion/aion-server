package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.hotspot.HotspotTemplate;

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
			hotspotTemplates = new ArrayList<>();
			return 0;
		}
		return hotspotTemplates.size();
	}

	public List<HotspotTemplate> getHotspotTemplates() {
		if (hotspotTemplates == null) {
			return new ArrayList<>();
		}
		return hotspotTemplates;
	}

	public HotspotTemplate getHotspotTemplateById(int id) {
		for (HotspotTemplate t : hotspotTemplates) {
			if (t.getId() == id)
				return t;
		}
		return null;
	}
}
