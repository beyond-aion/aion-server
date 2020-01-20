package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.worldraid.WorldRaidLocation;

/**
 * @author Alcapwnd, Whoop, Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "world_raid_locations")
public class WorldRaidData {

	@XmlElement(name = "world_raid_location")
	private List<WorldRaidLocation> worldRaidLocations;

	@XmlTransient
	private Map<Integer, WorldRaidLocation> locationsById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WorldRaidLocation location : worldRaidLocations)
			locationsById.putIfAbsent(location.getLocationId(), location);
		worldRaidLocations.clear();
		worldRaidLocations = null;
	}

	public WorldRaidLocation getLocationsById(int locationId) {
		return locationsById.get(locationId);
	}

	public Map<Integer, WorldRaidLocation> getLocations() {
		return locationsById;
	}

	public int size() {
		return locationsById.size();
	}

}
