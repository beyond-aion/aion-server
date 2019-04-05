package com.aionemu.gameserver.model.templates.spawns;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "house")
public class HouseSpawns {

	@XmlElement(name = "spawn", required = true)
	private List<HouseSpawn> spawns;

	@XmlAttribute(name = "address", required = true)
	private int address;

	public List<HouseSpawn> getSpawns() {
		return spawns == null ? Collections.emptyList() : spawns;
	}

	public int getAddress() {
		return address;
	}

}
