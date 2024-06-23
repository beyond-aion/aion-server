package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.events.PlayerEnteredEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerAllianceEnteredEvent extends PlayerEnteredEvent<PlayerAlliance> {

	public PlayerAllianceEnteredEvent(PlayerAlliance alliance, Player player) {
		super(alliance, player);
	}

	@Override
	public void handleEvent() {
		PlayerAllianceMember invitedMember = PlayerAllianceService.addPlayerToAlliance(team, player);

		SM_ALLIANCE_INFO allianceInfo = new SM_ALLIANCE_INFO(team);
		SM_ALLIANCE_MEMBER_INFO allianceMemberInfo = new SM_ALLIANCE_MEMBER_INFO(invitedMember, PlayerAllianceEvent.JOIN);
		PacketSendUtility.sendPacket(player, allianceInfo);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_ENTERED_FORCE());
		PacketSendUtility.sendPacket(player, allianceMemberInfo);
		team.sendBrands(player);
		team.forEachTeamMember(member -> {
			Player p = member.getObject();
			if (!player.equals(p)) {
				PacketSendUtility.sendPacket(p, allianceMemberInfo);
				PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_FORCE_HE_ENTERED_FORCE(player.getName()));
				PacketSendUtility.sendPacket(p, allianceInfo);
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(member, PlayerAllianceEvent.ENTER));
			}
		});
		PacketSendUtility.broadcastPacket(player, new SM_ABYSS_RANK_UPDATE(1, player), true);

		if (team.isInLeague()) {
			team.getLeague().broadcast();
		}
		super.handleEvent();
	}
}
