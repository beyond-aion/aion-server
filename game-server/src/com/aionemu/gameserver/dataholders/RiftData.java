package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.rift.RiftTemplate;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rift_locations")
public class RiftData {

	@XmlElement(name = "rift_location")
	private List<RiftTemplate> riftTemplates;
	@XmlTransient
	private LinkedHashMap<Integer, RiftLocation> rift = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (RiftTemplate template : riftTemplates) {
			rift.put(template.getId(), new RiftLocation(template));
		}
	}

	public int size() {
		return rift.size();
	}

	public LinkedHashMap<Integer, RiftLocation> getRiftLocations() {
		return rift;
	}

}
