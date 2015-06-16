package com.aionemu.gameserver.model.templates.npc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;

/**
 * @author ATracer
 */
@XmlType(name = "rating")
@XmlEnum
public enum NpcRating {
	JUNK(CreatureSeeState.NORMAL),
	NORMAL(CreatureSeeState.NORMAL),
	ELITE(CreatureSeeState.SEARCH1),
	HERO(CreatureSeeState.SEARCH2),
	LEGENDARY(CreatureSeeState.SEARCH2);

	private final CreatureSeeState congenitalSeeState;

	private NpcRating(CreatureSeeState congenitalSeeState) {
		this.congenitalSeeState = congenitalSeeState;
	}

	public CreatureSeeState getCongenitalSeeState() {
		return congenitalSeeState;
	}
}
