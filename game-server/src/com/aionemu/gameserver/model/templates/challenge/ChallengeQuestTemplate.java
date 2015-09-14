package com.aionemu.gameserver.model.templates.challenge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChallengeQuest")
public class ChallengeQuestTemplate {

	@XmlAttribute(required = true)
	protected int score;

	@XmlAttribute(name = "repeat_count", required = true)
	protected int repeatCount;

	@XmlAttribute(required = true)
	protected int id;

	public int getScore() {
		return this.score;
	}

	public int getRepeatCount() {
		return this.repeatCount;
	}

	public int getId() {
		return this.id;
	}

}
