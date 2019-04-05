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

import com.aionemu.gameserver.model.templates.spawns.HouseSpawn;
import com.aionemu.gameserver.model.templates.spawns.HouseSpawns;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "house_npcs")
public class HouseNpcsData {

	@XmlElement(name = "house")
	private List<HouseSpawns> houseSpawnsData;

	@XmlTransient
	private Map<Integer, List<HouseSpawn>> houseSpawnsByAddressId = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (HouseSpawns houseSpawns : houseSpawnsData)
			houseSpawnsByAddressId.put(houseSpawns.getAddress(), houseSpawns.getSpawns());
		houseSpawnsData = null;
	}

	public List<HouseSpawn> getSpawnsByAddress(int address) {
		return houseSpawnsByAddressId.get(address);
	}

	public int size() {
		return houseSpawnsByAddressId.values().stream().mapToInt(List::size).sum();
	}

}
