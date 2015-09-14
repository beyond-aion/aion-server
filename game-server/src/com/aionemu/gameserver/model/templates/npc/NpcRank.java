package com.aionemu.gameserver.model.templates.npc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "rank")
@XmlEnum
public enum NpcRank {
	NOVICE,
	DISCIPLINED,
	SEASONED,
	EXPERT,
	VETERAN,
	MASTER;
}
