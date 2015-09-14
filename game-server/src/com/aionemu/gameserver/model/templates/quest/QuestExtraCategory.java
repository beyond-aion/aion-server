package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
