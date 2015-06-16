package com.aionemu.gameserver.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sphere")
public class Sphere {

	@XmlAttribute
	protected Float x;
	@XmlAttribute
	protected Float y;
	@XmlAttribute
	protected Float z;
	@XmlAttribute
	protected Float r;

	public Sphere() {
	}

	public Sphere(float x, float y, float z, float radius) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = radius;
	}

	public Float getX() {
		return x;
	}

	public Float getY() {
		return y;
	}

	public Float getZ() {
		return z;
	}

	public Float getR() {
		return r;
	}
}
