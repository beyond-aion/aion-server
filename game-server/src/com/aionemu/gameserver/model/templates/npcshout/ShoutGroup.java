package com.aionemu.gameserver.model.templates.npcshout;

import java.util.List;

import jakarta.xml.bind.annotation.*;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShoutGroup", propOrder = { "shoutNpcs" })
public class ShoutGroup {

	@XmlElement(name = "shout_npcs", required = true)
	protected List<ShoutList> shoutNpcs;

	@XmlAttribute(name = "client_ai")
	protected String clientAi;

	public List<ShoutList> getShoutNpcs() {
		return shoutNpcs == null ? List.of() : shoutNpcs;
	}

	public String getClientAi() {
		return clientAi;
	}
}
