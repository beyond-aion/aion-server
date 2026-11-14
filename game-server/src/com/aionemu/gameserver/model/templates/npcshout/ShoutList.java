package com.aionemu.gameserver.model.templates.npcshout;

import java.util.List;

import jakarta.xml.bind.annotation.*;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShoutList", propOrder = { "npcShouts" })
public class ShoutList {

	@XmlElement(name = "shout", required = true)
	protected List<NpcShout> npcShouts;

	@XmlAttribute(name = "npc_ids", required = true)
	protected List<Integer> npcIds;

	@XmlAttribute(name = "restrict_world")
	protected int restrictWorld;

	public List<NpcShout> getNpcShouts() {
		return npcShouts == null ? List.of() : npcShouts;
	}

	public List<Integer> getNpcIds() {
		return npcIds == null ? List.of() : npcIds;
	}

	public int getRestrictWorld() {
		return restrictWorld;
	}
}
