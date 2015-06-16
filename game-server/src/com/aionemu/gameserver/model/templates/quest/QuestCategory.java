package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "QuestCategory")
@XmlEnum
public enum QuestCategory {
	QUEST(0),
	EVENT(1),
	MISSION(0),
	SIGNIFICANT(0),
	IMPORTANT(0),
	NON_COUNT(0),
	SEEN_MARKER(0),
	TASK(0),
	FACTION(0),
	CHALLENGE_TASK(0),
	PUBLIC(0),
	LEGION(0),
	PRIMARY(0);

	private int id;

	private QuestCategory(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
