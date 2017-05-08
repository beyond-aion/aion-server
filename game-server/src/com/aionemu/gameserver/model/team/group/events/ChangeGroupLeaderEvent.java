package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.events.ChangeLeaderEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ChangeGroupLeaderEvent extends ChangeLeaderEvent<PlayerGroup> {

	public ChangeGroupLeaderEvent(PlayerGroup team, Player eventPlayer) {
		super(team, eventPlayer);
	}

	public ChangeGroupLeaderEvent(PlayerGroup team) {
		super(team, null);
	}

	@Override
	public void handleEvent() {
		if (eventPlayer == null) {
			changeLeaderToNextAvailablePlayer();
		} else {
			changeLeaderTo(eventPlayer);
		}
	}

	@Override
	protected void changeLeaderTo(final Player player) {
		team.changeLeader(team.getMember(player.getObjectId()));
		team.forEach(member -> {
			PacketSendUtility.sendPacket(member, new SM_GROUP_INFO(team));
			if (!player.equals(member)) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_NEW_LEADER(player.getName()));
			} else {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_PARTY_YOU_BECOME_NEW_LEADER());
			}
		});
	}

}
