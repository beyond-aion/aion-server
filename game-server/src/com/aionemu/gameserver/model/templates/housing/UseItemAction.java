package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UseItemAction")
public class UseItemAction {

	@XmlAttribute(name = "final_reward_id")
	protected Integer finalRewardId;

	@XmlAttribute(name = "reward_id")
	protected Integer rewardId;

	@XmlAttribute(name = "remove_count")
	protected Integer removeCount;

	@XmlAttribute(name = "check_type")
	protected Integer checkType;

	public Integer getFinalRewardId() {
		return finalRewardId;
	}

	public Integer getRewardId() {
		return rewardId;
	}

	public Integer getRemoveCount() {
		return removeCount;
	}

	public Integer getCheckType() {
		return checkType;
	}

}
