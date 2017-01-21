package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.events.PlayerStopMentoringEvent;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerGroupStopMentoringEvent extends PlayerStopMentoringEvent<PlayerGroup> {

	/**
	 * @param group
	 * @param player
	 */
	public PlayerGroupStopMentoringEvent(PlayerGroup group, Player player) {
		super(group, player);
	}

	@Override
	protected void sendGroupPacketOnMentorEnd(Player member) {
		PacketSendUtility.sendPacket(member, new SM_GROUP_MEMBER_INFO(team, player, GroupEvent.MOVEMENT));
	}

}
