package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "QuestCategory")
@XmlEnum
public enum QuestCategory {
	QUEST,
	EVENT,
	MISSION,
	SIGNIFICANT,
	IMPORTANT,
	NON_COUNT,
	SEEN_MARKER,
	TASK,
	FACTION,
	CHALLENGE_TASK,
	PUBLIC,
	LEGION,
	PRIMARY;

	/**
	 * @return True if quests of this category count towards the basic quest limit (mission, task, faction, event, non_count and public don't).
	 */
	public boolean countsTowardsQuestLimit() {
		return switch (this) {
			case QUEST, SEEN_MARKER, IMPORTANT, SIGNIFICANT, CHALLENGE_TASK, LEGION -> true;
			default -> false;
		};
	}
}
