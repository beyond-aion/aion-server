package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerEnteredEvent implements TeamEvent {

	private final PlayerAlliance alliance;
	private final Player invited;
	private PlayerAllianceMember invitedMember;

	public PlayerEnteredEvent(PlayerAlliance alliance, Player player) {
		this.alliance = alliance;
		this.invited = player;
	}

	/**
	 * Entered player should not be in group yet
	 */
	@Override
	public boolean checkCondition() {
		return !alliance.hasMember(invited.getObjectId());
	}

	@Override
	public void handleEvent() {
		PlayerAllianceService.addPlayerToAlliance(alliance, invited);

		invitedMember = alliance.getMember(invited.getObjectId());

		PacketSendUtility.sendPacket(invited, new SM_ALLIANCE_INFO(alliance));
		PacketSendUtility.sendPacket(invited, new SM_SHOW_BRAND(0, 0, alliance.isInLeague()));
		PacketSendUtility.sendPacket(invited, SM_SYSTEM_MESSAGE.STR_FORCE_ENTERED_FORCE());
		PacketSendUtility.sendPacket(invited, new SM_ALLIANCE_MEMBER_INFO(invitedMember, PlayerAllianceEvent.JOIN));
		alliance.forEachTeamMember(member -> {
			Player player = member.getObject();
			if (!invited.equals(player)) {
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(invitedMember, PlayerAllianceEvent.JOIN));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_HE_ENTERED_FORCE(invited.getName()));
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_INFO(alliance));
				PacketSendUtility.sendPacket(player, new SM_SHOW_BRAND(0, 0, alliance.isInLeague()));
				PacketSendUtility.sendPacket(invited, new SM_ALLIANCE_MEMBER_INFO(member, PlayerAllianceEvent.ENTER));
			}
		});
		PacketSendUtility.broadcastPacket(invited, new SM_ABYSS_RANK_UPDATE(1, invited), true);

		if (alliance.isInLeague()) {
			alliance.getLeague().broadcast();
		}
	}
}
