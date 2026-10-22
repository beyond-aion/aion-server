package com.aionemu.gameserver.model.templates.road;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author SheppeR
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoadExit")
public class RoadExit {

	@XmlAttribute(name = "mapid")
	private int mapId;

	@XmlAttribute(name = "x")
	private float x;

	@XmlAttribute(name = "y")
	private float y;

	@XmlAttribute(name = "z")
	private float z;

	public int getMap() {
		return mapId;
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
}
