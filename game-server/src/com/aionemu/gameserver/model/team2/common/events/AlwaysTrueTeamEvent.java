package com.aionemu.gameserver.model.team2.common.events;

import com.aionemu.gameserver.model.team2.TeamEvent;

/**
 * @author ATracer
 */
public abstract class AlwaysTrueTeamEvent implements TeamEvent {

	@Override
	public final boolean checkCondition() {
		return true;
	}

}
