package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.events.PlayerLeavedEvent.LeaveReson;
import com.aionemu.gameserver.model.team.group.PlayerGroup;

/**
 * @author ATracer
 */
public class GroupDisbandEvent extends AlwaysTrueTeamEvent {

	private final PlayerGroup group;

	/**
	 * @param group
	 */
	public GroupDisbandEvent(PlayerGroup group) {
		this.group = group;
	}

	@Override
	public void handleEvent() {
		group.forEach(member -> group.onEvent(new PlayerGroupLeavedEvent(group, member, LeaveReson.DISBAND)));
	}

}
