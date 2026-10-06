package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

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
