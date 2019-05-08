package com.aionemu.gameserver.model.team.alliance.events;

import java.util.Objects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerDisconnectedEvent implements TeamEvent {

	private final PlayerAlliance alliance;
	private final Player disconnected;
	private final PlayerAllianceMember disconnectedMember;

	public PlayerDisconnectedEvent(PlayerAlliance alliance, Player player) {
		this.alliance = alliance;
		this.disconnected = player;
		this.disconnectedMember = alliance.getMember(disconnected.getObjectId());
	}

	/**
	 * Player should be in alliance before disconnection
	 */
	@Override
	public boolean checkCondition() {
		return alliance.hasMember(disconnected.getObjectId());
	}

	@Override
	public void handleEvent() {
		Objects.requireNonNull(disconnectedMember, "Disconnected member should not be null");
		Player leader = alliance.getLeaderObject();

		if (disconnected.equals(leader)) {
			alliance.onEvent(new ChangeAllianceLeaderEvent(alliance));
		}

		alliance.forEach(member -> {
			if (!disconnected.equals(member)) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_FORCE_HE_BECOME_OFFLINE(disconnected.getName()));
				PacketSendUtility.sendPacket(member, new SM_ALLIANCE_MEMBER_INFO(disconnectedMember, PlayerAllianceEvent.DISCONNECTED));
				PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(alliance));
			}
		});

		if (alliance.getOnlineMembers().isEmpty()) {
			PlayerAllianceService.disband(alliance, false);
		} else if (alliance.isInLeague()) {
			alliance.getLeague().broadcast(disconnected);
		}
	}

}
