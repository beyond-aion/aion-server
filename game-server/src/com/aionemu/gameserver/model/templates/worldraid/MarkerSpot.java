package com.aionemu.gameserver.model.templates.worldraid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerSpot")
public class MarkerSpot {

	@XmlAttribute(name = "x", required = true)
	private float x;
	@XmlAttribute(name = "y", required = true)
	private float y;
	@XmlAttribute(name = "z", required = true)
	private float z;
	@XmlAttribute(name = "h")
	private byte h = 0;

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

	@Override
	public String toString() {
		return "MarkerSpot[" + "x=" + x + ", y=" + y + ", z=" + z + ", h=" + h + ']';
	}
}
