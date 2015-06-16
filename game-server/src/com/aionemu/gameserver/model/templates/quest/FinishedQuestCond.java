package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FinishedQuest", propOrder = { "questId", "reward" })
public class FinishedQuestCond {

	@XmlAttribute(name = "quest_id", required = true)
	protected int questId;
	@XmlAttribute(name = "reward")
	protected int reward = -1;

	public int getQuestId() {
		return questId;
	}

	public int getReward() {
		return reward;
	}
}
