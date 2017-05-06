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
@XmlType(name = "PortalDialog")
public class PortalDialog {

	@XmlElement(name = "portal_path")
	private List<PortalPath> portalPaths;
	@XmlAttribute(name = "npc_id")
	private int npcId;
	@XmlAttribute(name = "teleport_dialog_id")
	private int teleportDialogId = 1011;

	public List<PortalPath> getPortalPaths() {
		return portalPaths;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getTeleportDialogId() {
		return teleportDialogId;
	}

}
