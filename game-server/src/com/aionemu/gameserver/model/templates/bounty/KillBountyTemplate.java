package com.aionemu.gameserver.model.templates.bounty;

import java.util.List;

import javax.xml.bind.annotation.*;

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
	@XmlAttribute(name = "is_random_reward")
	private boolean isRandomReward;

	@XmlElement(name = "bounty")
	private List<BountyTemplate> bounties;

	public BountyType getBountyType() {
		return type;
	}

	public int getKillCount() {
		return killCount;
	}

	public boolean isRandomReward() {
		return isRandomReward;
	}

	public List<BountyTemplate> getBounties() {
		return bounties;
	}
}
