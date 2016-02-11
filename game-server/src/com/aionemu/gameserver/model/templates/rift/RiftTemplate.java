package com.aionemu.gameserver.model.templates.rift;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rift")
public class RiftTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "world")
	protected int world;
	@XmlAttribute(name = "has_spawns")
	protected boolean hasSpawns;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return world;
	}

	public boolean hasSpawns() {
		return hasSpawns;
	}

}
