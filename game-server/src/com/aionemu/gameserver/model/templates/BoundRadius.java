package com.aionemu.gameserver.model.templates;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
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

	private float collision;

	public static final BoundRadius DEFAULT = new BoundRadius(0f, 0f, 0f);

	public BoundRadius() {
	}

	/**
	 * @param front
	 * @param side
	 * @param upper
	 */
	public BoundRadius(float front, float side, float upper) {
		this.front = front;
		this.side = side;
		this.upper = upper;
		calculateCollision(front, side);
	}

	/**
	 * @param u
	 * @param parent
	 */
	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		calculateCollision(front, side);
	}

	/**
	 * @param front
	 * @param side
	 */
	protected void calculateCollision(float front, float side) {
		this.collision = (float) Math.sqrt(side * front);
	}

	public float getFront() {
		return front;
	}

	public float getSide() {
		return side;
	}

	public float getUpper() {
		return upper;
	}

	public float getCollision() {
		return collision;
	}

}
