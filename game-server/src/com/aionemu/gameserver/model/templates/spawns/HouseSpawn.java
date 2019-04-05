package com.aionemu.gameserver.model.templates.spawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HouseSpawn")
public class HouseSpawn {

	@XmlAttribute(name = "x", required = true)
	protected float x;

	@XmlAttribute(name = "y", required = true)
	protected float y;

	@XmlAttribute(name = "z", required = true)
	protected float z;

	@XmlAttribute(name = "h")
	protected byte h;

	@XmlAttribute(name = "type", required = true)
	protected SpawnType type;

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public byte getH() {
		return h;
	}

	public SpawnType getType() {
		return type;
	}

}
