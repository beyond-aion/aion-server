package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public abstract class AbstractTeamPlayerEvent<T extends TemporaryPlayerTeam<?>> implements Predicate<Player>, TeamEvent {

	protected final T team;
	protected final Player eventPlayer;

	public AbstractTeamPlayerEvent(T team, Player eventPlayer) {
		this.team = team;
		this.eventPlayer = eventPlayer;
	}
}
