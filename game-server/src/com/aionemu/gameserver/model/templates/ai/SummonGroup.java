package com.aionemu.gameserver.model.templates.ai;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonGroup")
public class SummonGroup {

	@XmlAttribute(name = "npcId")
	private int npcId;
	@XmlAttribute(name = "x")
	private float x;
	@XmlAttribute(name = "y")
	private float y;
	@XmlAttribute(name = "z")
	private float z;
	@XmlAttribute(name = "h")
	private byte h;
	@XmlAttribute(name = "minCount")
	private int minCount = 1;
	@XmlAttribute(name = "maxCount")
	private int maxCount;
	@XmlAttribute(name = "distance")
	private float distance;
	@XmlAttribute(name = "schedule")
	private int schedule;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (minCount <= 0)
			throw new IllegalArgumentException("minCount (" + minCount + ") for npc group " + npcId + " must be greater than zero");
		if (maxCount == 0)
			maxCount = minCount;
		else if (maxCount < minCount)
			throw new IllegalArgumentException("maxCount (" + maxCount + ") for npc group " + npcId + " must be greater than minCount (" + minCount + ")");
	}

	public int getNpcId() {
		return npcId;
	}

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

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public float getDistance() {
		return distance;
	}

	public int getSchedule() {
		return schedule;
	}
}
