package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;

/**
 * @author ATracer
 */
public abstract class ChangeLeaderEvent<T extends TemporaryPlayerTeam<?>> extends AbstractTeamPlayerEvent<T> {

	public ChangeLeaderEvent(T team, Player eventPlayer) {
		super(team, eventPlayer);
	}

	/**
	 * New leader either is null or should be online
	 */
	@Override
	public boolean checkCondition() {
		return eventPlayer == null || eventPlayer.isOnline();
	}

	protected final void changeLeaderToNextAvailablePlayer() {
		team.applyOnMembers(member -> {
			if (member.isOnline() && !member.equals(team.getLeader().getObject())) {
				changeLeaderTo(member);
				return false;
			}
			return true;
		});
	}

	protected abstract void changeLeaderTo(Player player);

}
