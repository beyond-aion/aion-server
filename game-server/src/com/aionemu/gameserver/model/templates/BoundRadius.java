package com.aionemu.gameserver.model.templates;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Collision cylinder as the client receives it: {@code front} and {@code side} are radii, {@code upper} is the full
 * height.
 *
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundRadius")
public class BoundRadius {

	@XmlAttribute
	private float front;
	@XmlAttribute
	private float side;
	@XmlAttribute
	private float upper;

	public static final BoundRadius DEFAULT = new BoundRadius(0f, 0f, 0f);
	public static final BoundRadius PLAYER = new BoundRadius(0.25f, 0.25f, 2f);

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

	public boolean isZero() {
		return front == 0 && side == 0 && upper == 0;
	}

}
