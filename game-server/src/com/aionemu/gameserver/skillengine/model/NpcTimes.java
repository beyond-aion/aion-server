package com.aionemu.gameserver.skillengine.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The hit points and length of a motion on an NPC model, taken from the model's animation for one weapon token.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcTimes")
public class NpcTimes {

	@XmlAttribute(name = "mesh", required = true)
	private String mesh;

	@XmlAttribute(name = "weapon", required = true)
	private String weapon;

	@XmlList
	@XmlAttribute(name = "hit_times", required = true)
	private List<Integer> hitTimesMillis;

	@XmlAttribute(name = "length")
	private Integer lengthMillis;

	/**
	 * @return Name of the NPC model, lowercase
	 */
	public String getMesh() {
		return mesh;
	}

	/**
	 * @return The weapon token of the animation names, like {@code 2hand} or {@code noweapon}
	 */
	public String getWeapon() {
		return weapon;
	}

	/**
	 * @return Milliseconds from the start of the motion to its hit point, one entry per animation variant starting at 1
	 */
	public List<Integer> getHitTimesMillis() {
		return hitTimesMillis;
	}

	/**
	 * @return Length of the animation in milliseconds, or null if not given (auto attacks)
	 */
	public Integer getLengthMillis() {
		return lengthMillis;
	}
}
