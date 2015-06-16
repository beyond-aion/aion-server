package com.aionemu.gameserver.model.team2.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.common.events.PlayerLeavedEvent.LeaveReson;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class GroupDisbandEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

	private final PlayerGroup group;

	/**
	 * @param group
	 */
	public GroupDisbandEvent(PlayerGroup group) {
		this.group = group;
	}

	@Override
	public void handleEvent() {
		group.applyOnMembers(this);
	}

	@Override
	public boolean apply(Player player) {
		group.onEvent(new PlayerGroupLeavedEvent(group, player, LeaveReson.DISBAND));
		return true;
	}

}
