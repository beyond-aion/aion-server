package com.aionemu.gameserver.questEngine.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author MrPoke
 */
@XmlEnum
public enum QuestStatus {

	START(3), // Accepted quests
	REWARD(4), // The quests, that are finished. "Go and get your reward"
	COMPLETE(5), // Completed quests
	LOCKED(6); // Not (yet) available quests

	private int id;

	private QuestStatus(int id) {
		this.id = id;
	}

	public int value() {
		return id;
	}
}
