package com.aionemu.gameserver.model.templates.worldraid;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorldRaidNpc")
public class WorldRaidNpc {

	@XmlAttribute(name = "npc_id", required = true)
	private int npcId = 0;
	@XmlAttribute(name = "death_msg_id")
	private Integer deathMsgId = 0;

	public int getNpcId() {
		return npcId;
	}

	public Integer getDeathMsgId() {
		return deathMsgId;
	}

}
