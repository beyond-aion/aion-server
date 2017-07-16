package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spawn_npc")
public class NpcSkillSpawn {

	@XmlAttribute(name = "npc_id")
	private int npcId;
	@XmlAttribute(name = "delay")
	private int delay;
	@XmlAttribute(name = "min_distance")
	private int minDistance;
	@XmlAttribute(name = "max_distance")
	private int maxDistance;
	@XmlAttribute(name = "min_count")
	private int minCount = 1;
	@XmlAttribute(name = "max_count")
	private int maxCount = 0;

	public int getNpcId() {
		return npcId;
	}

	public int getDelay() {
		return delay;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

}
