package com.aionemu.gameserver.model.templates.teleport;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author orz
 */
@XmlRootElement(name = "teleporter_template")
@XmlAccessorType(XmlAccessType.NONE)
public class TeleporterTemplate {

	@XmlAttribute(name = "npc_ids")
	private List<Integer> npcIds;
	@XmlAttribute(name = "teleportId", required = true)
	private int teleportId = 0;
	@XmlElement(name = "locations")
	private TeleLocIdData teleLocIdData;

	/**
	 * @return the npcId
	 */
	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public boolean containNpc(int npcId) {
		return npcIds.contains(npcId);
	}

	/**
	 * @return the teleportId
	 */
	public int getTeleportId() {
		return teleportId;
	}

	/**
	 * @return the teleLocIdData
	 */
	public TeleLocIdData getTeleLocIdData() {
		return teleLocIdData;
	}
}
