package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.event.EventService;

/**
 * @author Neon
 */
public abstract class PlayerEnteredEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> implements TeamEvent {

	protected final T team;
	protected final Player player;

	public PlayerEnteredEvent(T team, Player player) {
		this.team = team;
		this.player = player;
	}

	/**
	 * Entered player should not be in team yet
	 */
	@Override
	public boolean checkCondition() {
		return !team.hasMember(player.getObjectId());
	}

	@Override
	public void handleEvent() {
		EventService.getInstance().onEnteredTeam(player, team);
	}
}
