package com.aionemu.gameserver.model.templates.portal;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
