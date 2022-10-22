package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats
 */
@XmlType(name = "NpcSkillTargetAttribute")
@XmlEnum
public enum NpcSkillTargetAttribute {

	FRIEND,
	ME,
	MOST_HATED,
	RANDOM,
	RANDOM_EXCEPT_MOST_HATED,
	NONE;

}
