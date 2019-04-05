package com.aionemu.gameserver.model.templates.housing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Land", propOrder = { "addresses", "buildings", "sale", "fee", "caps" })
public class HousingLand {

	@XmlElementWrapper(name = "addresses", required = true)
	@XmlElement(name = "address")
	private List<HouseAddress> addresses;

	@XmlElementWrapper(name = "buildings", required = true)
	@XmlElement(name = "building")
	private List<Building> buildings;

	@XmlElement(required = true)
	private Sale sale;

	@XmlElement(required = true)
	private long fee;

	@XmlElement(required = true)
	private BuildingCapabilities caps;

	@XmlAttribute(name = "sign_nosale", required = true)
	private int signNosale;

	@XmlAttribute(name = "sign_sale", required = true)
	private int signSale;

	@XmlAttribute(name = "sign_waiting", required = true)
	private int signWaiting;

	@XmlAttribute(name = "sign_home", required = true)
	private int signHome;

	@XmlAttribute(name = "manager_npc", required = true)
	private int managerNpc;

	@XmlAttribute(name = "teleport_npc", required = true)
	private int teleportNpc;

	@XmlAttribute(required = true)
	private int id;

	public List<HouseAddress> getAddresses() {
		return addresses;
	}

	public List<Building> getBuildings() {
		return buildings;
	}

	public Building getDefaultBuilding() {
		for (Building building : buildings) {
			if (building.isDefault())
				return building;
		}
		return buildings.get(0); // fail
	}

	public Sale getSaleOptions() {
		return sale;
	}

	public long getMaintenanceFee() {
		return fee;
	}

	public BuildingCapabilities getCapabilities() {
		return caps;
	}

	public int getNosaleSignNpcId() {
		return signNosale;
	}

	public int getSaleSignNpcId() {
		return signSale;
	}

	public int getWaitingSignNpcId() {
		return signWaiting;
	}

	public int getHomeSignNpcId() {
		return signHome;
	}

	public int getManagerNpcId() {
		return managerNpc;
	}

	public int getTeleportNpcId() {
		return teleportNpc;
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

}
