package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author ATracer
 */
public class PlayerGroupUpdateEvent extends AlwaysTrueTeamEvent {

	private final PlayerGroup group;
	private final Player player;
	private final GroupEvent groupEvent;
	private final int slot;

	public PlayerGroupUpdateEvent(PlayerGroup group, Player player, GroupEvent groupEvent, int slot) {
		this.group = group;
		this.player = player;
		this.groupEvent = groupEvent;
		this.slot = slot;
	}

	public PlayerGroupUpdateEvent(PlayerGroup group, Player player, GroupEvent groupEvent) {
		this(group, player, groupEvent, 0);
	}

	@Override
	public void handleEvent() {
		group.sendPacket(Predicates.Players.allExcept(player), new SM_GROUP_MEMBER_INFO(group, player, groupEvent, slot));
	}

}
