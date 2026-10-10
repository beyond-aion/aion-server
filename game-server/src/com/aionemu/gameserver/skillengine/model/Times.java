package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author kecims
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Times")
public class Times {

	@XmlAttribute(name = "weapon")
	private String weapon;

	@XmlAttribute(name = "id")
	private int id;

	@XmlAttribute(name = "min")
	private float minTime;

	@XmlAttribute(name = "max")
	private float maxTime;

	@XmlAttribute(name = "animation_length")
	private float animationLength;

	@XmlAttribute(name = "hitpoints")
	private boolean hitpoints = true;

	@XmlAttribute(name = "cast_animation")
	private boolean castAnimation;

	@XmlAttribute
	private String source;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		weapon = weapon.intern();
	}

	public int getId() {
		return id;
	}

	public float getMinTime() {
		return minTime;
	}

	public float getMaxTime() {
		return maxTime;
	}

	public float getAnimationLength() {
		return animationLength;
	}

	public String getWeapon() {
		return weapon;
	}

	public boolean hasHitpoints() {
		return hitpoints;
	}

	public boolean isCastAnimation() {
		return castAnimation;
	}

}
