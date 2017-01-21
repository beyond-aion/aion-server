package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.events.PlayerLeavedEvent.LeaveReson;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class AllianceDisbandEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

	private final PlayerAlliance alliance;

	/**
	 * @param alliance
	 */
	public AllianceDisbandEvent(PlayerAlliance alliance) {
		this.alliance = alliance;
	}

	@Override
	public void handleEvent() {
		alliance.applyOnMembers(this);
	}

	@Override
	public boolean apply(Player player) {
		alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, player, LeaveReson.DISBAND));
		return true;
	}

}
