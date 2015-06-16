package com.aionemu.gameserver.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cylinder")
public class Cylinder {

	@XmlAttribute
	protected Float top;
	@XmlAttribute
	protected Float bottom;
	@XmlAttribute
	protected Float x;
	@XmlAttribute
	protected Float y;
	@XmlAttribute
	protected Float r;

	public Cylinder() {
	}

	public Cylinder(float x, float y, float radius, float top, float bottom) {
		this.x = x;
		this.y = y;
		this.r = radius;
		this.top = top;
		this.bottom = bottom;
	}

	public Float getTop() {
		return top;
	}

	public Float getBottom() {
		return bottom;
	}

	public Float getX() {
		return x;
	}

	public Float getY() {
		return y;
	}

	public Float getR() {
		return r;
	}
}
