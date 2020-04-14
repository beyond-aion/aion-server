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
	private int id;
	@XmlAttribute(name = "world")
	private int world;
	@XmlAttribute(name = "has_spawns")
	private boolean hasSpawns;
	@XmlAttribute(name = "auto_closeable")
	private boolean autoCloseable = true;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return world;
	}

	public boolean hasSpawns() {
		return hasSpawns;
	}

	public boolean isAutoCloseable() {
		return autoCloseable;
	}
}
