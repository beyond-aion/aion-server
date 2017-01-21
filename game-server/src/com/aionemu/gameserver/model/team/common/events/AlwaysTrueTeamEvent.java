package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.team.TeamEvent;

/**
 * @author ATracer
 */
public abstract class AlwaysTrueTeamEvent implements TeamEvent {

	@Override
	public final boolean checkCondition() {
		return true;
	}

}
