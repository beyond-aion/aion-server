package com.aionemu.gameserver.skillengine.model;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcMotionTime")
public class NpcMotionTime extends Times {
	@XmlList
	@XmlAttribute(name = "npc_ids", required = true)
	private List<Integer> npcIds;

	public List<Integer> getNpcIds() {
		return npcIds;
	}
}
