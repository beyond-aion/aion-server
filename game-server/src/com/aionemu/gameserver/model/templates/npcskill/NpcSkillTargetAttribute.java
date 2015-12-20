package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats
 *
 */
@XmlType(name = "NpcSkillTargetAttribute")
@XmlEnum
public enum NpcSkillTargetAttribute {

	ME,
	MOST_HATED, 
	NONE;
	
}
