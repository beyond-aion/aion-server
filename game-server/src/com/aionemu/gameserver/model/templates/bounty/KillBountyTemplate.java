package com.aionemu.gameserver.model.templates.bounty;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillBounty")
public class KillBountyTemplate {

	@XmlAttribute(name = "type")
	private BountyType type;
	@XmlAttribute(name = "kill_count")
	private int killCount;
	
	@XmlElement(name = "bounty")
	private List<BountyTemplate> bounties;
	
	public BountyType getBountyType() {
		return type;
	}
	
	public int getKillCount() {
		return killCount;
	}
	
	public List<BountyTemplate> getBounties() {
		return bounties;
	}
}
