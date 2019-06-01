package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HousingLand;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "lands" })
@XmlRootElement(name = "house_lands")
public class HouseData {

	@XmlElement(name = "land")
	private List<HousingLand> lands;

	@XmlTransient
	private Map<Integer, HouseAddress> addressesById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (HousingLand land : lands) {
			for (HouseAddress address : land.getAddresses()) {
				if (addressesById.put(address.getId(), address) != null)
					throw new IllegalArgumentException("Duplicate house address " + address.getId() + " in house_lands templates");
			}
		}
	}

	public List<HouseAddress> getAddresses(int worldId) {
		return addressesById.values().stream().filter(address -> address.getMapId() == worldId).collect(Collectors.toList());
	}

	public HouseAddress getAddress(int houseAddress) {
		return addressesById.get(houseAddress);
	}

	public HouseAddress getStudioAddress(Race race) {
		return getAddress(race == Race.ELYOS ? 2001 : 3001);
	}

	public Collection<HousingLand> getLands() {
		return lands;
	}

	public int size() {
		return lands.size();
	}
}
