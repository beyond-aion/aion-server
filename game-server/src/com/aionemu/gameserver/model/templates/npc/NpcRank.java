package com.aionemu.gameserver.model.templates.npc;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

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
