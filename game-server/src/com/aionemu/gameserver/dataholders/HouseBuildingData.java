package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.housing.Building;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "buildings" })
public class HouseBuildingData {

	@XmlElement(name = "building")
	private List<Building> buildings;

	@XmlTransient
	private Map<Integer, Building> buildingById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (buildings == null)
			return;

		for (Building building : buildings) {
			if (buildingById.put(building.getId(), building) != null)
				throw new IllegalArgumentException("Duplicate building ID " + building.getId());
		}
		buildings = null;
	}

	public Building getBuilding(int buildingId) {
		return buildingById.get(buildingId);
	}

	public int size() {
		return buildingById.size();
	}
}
