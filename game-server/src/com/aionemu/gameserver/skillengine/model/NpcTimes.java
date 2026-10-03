package com.aionemu.gameserver.skillengine.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 * The hit point and length of a motion variant on the model of the listed NPCs, taken from the animation for the weapon they hold.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcTimes")
public class NpcTimes {

	@XmlList
	@XmlAttribute(name = "npc_ids", required = true)
	private List<Integer> npcIds;

	@XmlAttribute(name = "id")
	private int id = 1;

	@XmlAttribute(name = "hit_time", required = true)
	private int hitTimeMillis;

	@XmlAttribute(name = "length")
	private Integer lengthMillis;

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	/**
	 * @return Number of the animation variant, starting at 1
	 */
	public int getId() {
		return id;
	}

	public int getHitTimeMillis() {
		return hitTimeMillis;
	}

	/**
	 * @return Length of the animation in milliseconds, or null if not given (auto attacks)
	 */
	public Integer getLengthMillis() {
		return lengthMillis;
	}
}
