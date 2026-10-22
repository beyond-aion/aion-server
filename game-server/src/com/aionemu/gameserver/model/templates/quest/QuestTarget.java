package com.aionemu.gameserver.model.templates.quest;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * @author Rolandas
 */
@XmlEnum
public enum QuestTarget {
	NONE,
	AREA,
	LEAGUE,
	ALLIANCE
}
