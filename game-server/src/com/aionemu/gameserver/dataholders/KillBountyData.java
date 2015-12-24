package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.bounty.KillBountyTemplate;

/**
 * @author Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "kill_bounties")
public class KillBountyData {

	@XmlElement(name = "kill_bounty")
	private List<KillBountyTemplate> killBounties;

	public int size() {
		return killBounties.size();
	}

	public List<KillBountyTemplate> getKillBounties() {
		return killBounties;
	}
}
