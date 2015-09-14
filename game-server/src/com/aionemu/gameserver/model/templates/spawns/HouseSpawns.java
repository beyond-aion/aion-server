package com.aionemu.gameserver.model.templates.spawns;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "spawns" })
@XmlRootElement(name = "house")
public class HouseSpawns implements Comparable<HouseSpawns> {

	@XmlElement(name = "spawn", required = true)
	protected List<HouseSpawn> spawns;

	@XmlAttribute(name = "address", required = true)
	protected int address;

	public List<HouseSpawn> getSpawns() {
		if (spawns == null) {
			spawns = new ArrayList<HouseSpawn>();
		}
		return this.spawns;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int value) {
		this.address = value;
	}

	@Override
	public int compareTo(HouseSpawns o) {
		return o.address - address;
	}

}
