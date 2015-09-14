package com.aionemu.gameserver.model.templates.challenge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChallengeReward")
public class ChallengeReward {

	@XmlAttribute(name = "msg_id")
	protected Integer msgId;

	@XmlAttribute
	protected Integer value;

	@XmlAttribute(required = true)
	protected RewardType type;

	public Integer getMsgId() {
		return this.msgId;
	}

	public Integer getValue() {
		return this.value;
	}

	public RewardType getType() {
		return this.type;
	}
}
