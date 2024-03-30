package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerGroupInvite extends RequestResponseHandler<Player> {

	public PlayerGroupInvite(Player inviter) {
		super(inviter);
	}

	@Override
	public void acceptRequest(Player inviter, Player invited) {
		if (PlayerRestrictions.canInviteToGroup(inviter, invited)) {
			PlayerGroup group = inviter.getPlayerGroup();
			if (group != null) {
				PlayerGroupService.addPlayer(group, invited);
			} else {
				PlayerGroupService.createGroup(inviter, invited, TeamType.GROUP, 0);
			}
		}
	}

	@Override
	public void denyRequest(Player inviter, Player invited) {
		PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_PARTY_HE_REJECT_INVITATION(invited.getName()));
	}

}
