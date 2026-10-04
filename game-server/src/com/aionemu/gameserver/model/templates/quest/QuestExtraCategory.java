package com.aionemu.gameserver.model.templates.quest;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Cheatkiller
 */
@XmlType(name = "QuestExtraCategory")
@XmlEnum
public enum QuestExtraCategory {
	NONE,
	COIN_QUEST,
	DRACONIC_RECIPE_QUEST, // not use 3.9
	DEVANION_QUEST,
	GOLD_QUEST;

}
