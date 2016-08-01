package com.aionemu.gameserver.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalPath")
public class PortalPath {

	@XmlElement(name = "portal_req")
	protected PortalReq portalReq;
	@XmlAttribute(name = "dialog")
	protected int dialog;
	@XmlAttribute(name = "loc_id")
	protected int locId;
	@XmlAttribute(name = "player_count")
	protected int playerCount = 1;
	@XmlAttribute(name = "instance")
	protected boolean instance;
	@XmlAttribute(name = "siege_id")
	protected int siegeId;
	@XmlAttribute(name = "race")
	protected Race race = Race.PC_ALL;
	@XmlAttribute(name = "err_group")
	protected int errGroup;
	@XmlAttribute(name = "min_rank")
	protected int minRank;

	public PortalReq getPortalReq() {
		return portalReq;
	}

	public int getDialog() {
		return dialog;
	}

	public int getLocId() {
		return locId;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public boolean isInstance() {
		return instance;
	}

	public int getSiegeId() {
		return siegeId;
	}

	public Race getRace() {
		return race;
	}

	public int getErrGroup() {
		return errGroup;
	}

	public int getMinRank() {
		return minRank;
	}
}
