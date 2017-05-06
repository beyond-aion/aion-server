package com.aionemu.gameserver.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestReq")
public class QuestReq {

	@XmlAttribute(name = "quest_id")
	protected int questId;
	@XmlAttribute(name = "quest_step")
	protected int questStep;

	public int getQuestId() {
		return questId;
	}

	public void setQuestId(int value) {
		this.questId = value;
	}

	public int getQuestStep() {
		return questStep;
	}

	public void setQuestStep(int value) {
		this.questStep = value;
	}

}
