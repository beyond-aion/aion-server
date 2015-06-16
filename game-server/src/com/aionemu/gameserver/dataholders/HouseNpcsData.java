package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.HouseSpawn;
import com.aionemu.gameserver.model.templates.spawns.HouseSpawns;

/**
 * @author Rolandas
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "houseSpawnsData" })
@XmlRootElement(name = "house_npcs")
public class HouseNpcsData {

	@XmlElement(name = "house")
	protected List<HouseSpawns> houseSpawnsData;

	public List<HouseSpawns> getHouseSpawns() {
		if (houseSpawnsData == null) {
			houseSpawnsData = new ArrayList<HouseSpawns>();
		}
		return this.houseSpawnsData;
	}

	@XmlTransient
	private TIntObjectHashMap<List<HouseSpawn>> houseSpawnsByAddressId = new TIntObjectHashMap<List<HouseSpawn>>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (HouseSpawns houseSpawns : getHouseSpawns()) {
			houseSpawnsByAddressId.put(houseSpawns.getAddress(), houseSpawns.getSpawns());
		}
	}
	
	public List<HouseSpawn> getSpawnsByAddress(int address) {
		return houseSpawnsByAddressId.get(address);
	}

	public int size() {
		return houseSpawnsByAddressId.size() * 3;
	}

}
