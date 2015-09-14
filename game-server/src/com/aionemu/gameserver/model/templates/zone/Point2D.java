package com.aionemu.gameserver.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Point2D")
public class Point2D {

	@XmlAttribute(name = "y")
	protected float y;
	@XmlAttribute(name = "x")
	protected float x;

	/**
	 * @param x
	 * @param y
	 */
	public Point2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point2D() {
		super();
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}
}
