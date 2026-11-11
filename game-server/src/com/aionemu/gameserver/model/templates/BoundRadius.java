package com.aionemu.gameserver.model.templates;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Collision cylinder as the client receives it: {@code front} and {@code side} are radii, {@code upper} is the full
 * height. Attributes missing in the XML keep the default creature cylinder; an explicit zero stays zero.
 *
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundRadius")
public class BoundRadius {

	@XmlAttribute
	private float front = 0.25f;
	@XmlAttribute
	private float side = 0.25f;
	@XmlAttribute
	private float upper = 2f;

	/** Cylinder of a creature that defines none of its own. */
	public static final BoundRadius DEFAULT = new BoundRadius();
	/** No cylinder at all. */
	public static final BoundRadius ZERO = new BoundRadius(0f, 0f, 0f);

	public BoundRadius() {
	}

	public BoundRadius(float front, float side, float upper) {
		this.front = front;
		this.side = side;
		this.upper = upper;
	}

	public float getFront() {
		return front;
	}

	public float getSide() {
		return side;
	}

	public float getMaxOfFrontAndSide() {
		return Math.max(front, side);
	}

	public float getUpper() {
		return upper;
	}

}
