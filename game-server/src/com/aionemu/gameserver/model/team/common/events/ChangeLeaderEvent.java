package com.aionemu.gameserver.model.team.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.group.events.ChangeGroupLeaderEvent;

/**
 * @author ATracer
 */
public abstract class ChangeLeaderEvent<T extends TemporaryPlayerTeam<?>> extends AbstractTeamPlayerEvent<T> {

	private static final Logger log = LoggerFactory.getLogger(ChangeGroupLeaderEvent.class);

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

	/**
	 * @param oldLeader
	 * @return checked result
	 */
	protected boolean checkLeaderChanged(Player oldLeader) {
		boolean result = team.isLeader(oldLeader);
		if (result) {
			log.info("TEAM: leader is not changed, total: {}, online: {}", team.size(), team.onlineMembers());
		}
		return result;
	}

	protected abstract void changeLeaderTo(Player player);

}
