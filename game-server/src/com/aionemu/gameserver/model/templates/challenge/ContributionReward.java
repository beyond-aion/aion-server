package com.aionemu.gameserver.model.templates.challenge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContributionReward")
public class ContributionReward {

	@XmlAttribute(name = "item_count", required = true)
	protected int itemCount;

	@XmlAttribute(name = "reward_id", required = true)
	protected int rewardId;

	@XmlAttribute(required = true)
	protected int number;

	@XmlAttribute(required = true)
	protected int rank;

	public int getItemCount() {
		return this.itemCount;
	}

	public int getRewardId() {
		return this.rewardId;
	}

	public int getNumber() {
		return this.number;
	}

	public int getRank() {
		return this.rank;
	}
}
