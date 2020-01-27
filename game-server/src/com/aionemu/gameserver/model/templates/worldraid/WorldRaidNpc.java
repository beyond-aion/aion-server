package com.aionemu.gameserver.model.templates.worldraid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
