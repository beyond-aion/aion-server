package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.events.PlayerLeavedEvent.LeaveReson;

/**
 * @author ATracer
 */
public class AllianceDisbandEvent extends AlwaysTrueTeamEvent {

	private final PlayerAlliance alliance;

	/**
	 * @param alliance
	 */
	public AllianceDisbandEvent(PlayerAlliance alliance) {
		this.alliance = alliance;
	}

	@Override
	public void handleEvent() {
		alliance.forEach(player -> alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, player, LeaveReson.DISBAND)));
	}

}
