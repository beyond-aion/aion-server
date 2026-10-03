package com.aionemu.gameserver.model.templates.hotspot;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Hotspot")
public class HotspotTemplate {

	@XmlAttribute(name = "id")
	protected int id;

	@XmlAttribute(name = "worldId")
	protected int worldId;

	@XmlAttribute(name = "x")
	protected float x;

	@XmlAttribute(name = "y")
	protected float y;

	@XmlAttribute(name = "z")
	protected float z;

	@XmlAttribute(name = "race")
	protected Race race;

	@XmlAttribute(name = "price")
	protected long price;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return worldId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public Race getRace() {
		return race;
	}

	public long getPrice() {
		return price;
	}
}
