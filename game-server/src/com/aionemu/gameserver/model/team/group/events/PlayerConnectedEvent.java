package com.aionemu.gameserver.model.team.group.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerConnectedEvent extends AlwaysTrueTeamEvent {

	private static final Logger log = LoggerFactory.getLogger(PlayerConnectedEvent.class);
	private final PlayerGroup group;
	private final Player player;

	public PlayerConnectedEvent(PlayerGroup group, Player player) {
		this.group = group;
		this.player = player;
	}

	@Override
	public void handleEvent() {
		group.removeMember(player.getObjectId());
		group.addMember(new PlayerGroupMember(player));
		if (player.equals(group.getLeader().getObject())) {
			log.warn("[TEAM] leader ({}) reconnected, but should have lost leadership on disconnect", player);
			group.changeLeader(new PlayerGroupMember(player));
		}
		PacketSendUtility.sendPacket(player, new SM_GROUP_INFO(group));
		PacketSendUtility.sendPacket(player, new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.JOIN));
		group.sendBrands(player);
		group.forEach(member -> {
			if (!player.equals(member)) {
				PacketSendUtility.sendPacket(member, new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.ENTER));
				PacketSendUtility.sendPacket(player, new SM_GROUP_MEMBER_INFO(group, member, GroupEvent.ENTER));
			}
		});
		// change leader to player logging in first if all players are disconnected
		if (group.getLeader() != null && !group.hasMember(group.getLeader().getObjectId()))
			group.onEvent(new ChangeGroupLeaderEvent(group, player));
	}

}
