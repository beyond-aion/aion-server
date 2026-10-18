package com.aionemu.gameserver.model.templates.spawns.riftspawns;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.Spawn;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RiftSpawn")
public class RiftSpawn {

	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "world")
	private int world;
	@XmlElement(name = "spawn")
	private List<Spawn> spawns = new ArrayList<>();

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return world;
	}

	public List<Spawn> getSpawns() {
		return spawns;
	}

}
