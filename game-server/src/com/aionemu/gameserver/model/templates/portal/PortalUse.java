package com.aionemu.gameserver.model.templates.portal;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalUse")
public class PortalUse {

	@XmlElement(name = "portal_path")
	private List<PortalPath> portalPaths;
	@XmlAttribute(name = "npc_id")
	private int npcId;

	public List<PortalPath> getPortalPaths() {
		return portalPaths;
	}

	public int getNpcId() {
		return npcId;
	}

}
