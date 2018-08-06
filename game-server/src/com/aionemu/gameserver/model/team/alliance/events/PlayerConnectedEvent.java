package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerConnectedEvent extends AlwaysTrueTeamEvent {

	private final PlayerAlliance alliance;
	private final Player connected;

	public PlayerConnectedEvent(PlayerAlliance alliance, Player player) {
		this.alliance = alliance;
		this.connected = player;
	}

	@Override
	public void handleEvent() {
		alliance.removeMember(connected.getObjectId());
		PlayerAllianceMember connectedMember = new PlayerAllianceMember(connected);
		alliance.addMember(connectedMember);

		PacketSendUtility.sendPacket(connected, new SM_ALLIANCE_INFO(alliance));
		PacketSendUtility.sendPacket(connected, new SM_ALLIANCE_MEMBER_INFO(connectedMember, PlayerAllianceEvent.RECONNECT));
		alliance.sendBrands(connected);

		alliance.forEachTeamMember(member -> {
			Player player = member.getObject();
			if (!connected.equals(player)) {
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(connectedMember, PlayerAllianceEvent.RECONNECT));
				PacketSendUtility.sendPacket(connected, new SM_ALLIANCE_MEMBER_INFO(member, PlayerAllianceEvent.RECONNECT));
			}
		});
	}

}
