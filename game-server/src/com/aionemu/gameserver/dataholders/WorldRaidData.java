package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

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
