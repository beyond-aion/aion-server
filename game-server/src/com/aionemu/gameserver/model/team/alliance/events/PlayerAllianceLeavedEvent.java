package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.events.PlayerLeavedEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEAVE_GROUP_MEMBER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PlayerAllianceLeavedEvent extends PlayerLeavedEvent<PlayerAllianceMember, PlayerAlliance> {

	public PlayerAllianceLeavedEvent(PlayerAlliance alliance, Player player) {
		super(alliance, player);
	}

	public PlayerAllianceLeavedEvent(PlayerAlliance team, Player player, PlayerLeavedEvent.LeaveReson reason, String banPersonName) {
		super(team, player, reason, banPersonName);
	}

	public PlayerAllianceLeavedEvent(PlayerAlliance alliance, Player player, PlayerLeavedEvent.LeaveReson reason) {
		super(alliance, player, reason);
	}

	@Override
	public void handleEvent() {
		team.getViceCaptainIds().remove(leavedPlayer.getObjectId());

		if (reason != LeaveReson.DISBAND && team.isLeader(leavedPlayer))
			team.onEvent(new ChangeAllianceLeaderEvent(team));

		PlayerAllianceMember leavedTeamMember = team.removeMember(leavedPlayer.getObjectId());

		SM_SYSTEM_MESSAGE leaveMsg;
		switch (reason) {
			case LEAVE:
				leaveMsg = SM_SYSTEM_MESSAGE.STR_FORCE_LEAVE_HIM(leavedPlayer.getName());
				break;
			case LEAVE_TIMEOUT:
				leaveMsg = SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_LEAVED_PARTY_OFFLINE_TIMEOUT(leavedPlayer.getName());
				break;
			case BAN:
				leaveMsg = SM_SYSTEM_MESSAGE.STR_FORCE_BAN_HIM(banPersonName, leavedPlayer.getName());
				break;
			case DISBAND:
			default:
				leaveMsg = SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED();
				break;
		}
		team.forEach(player -> {
			PacketSendUtility.sendPacket(player, leaveMsg);
			if (reason != LeaveReson.DISBAND) {
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(leavedTeamMember, PlayerAllianceEvent.LEAVE));
				PacketSendUtility.sendPacket(player, new SM_ALLIANCE_INFO(team));
				PacketSendUtility.sendPacket(player, new SM_SHOW_BRAND(0, 0, team.isInLeague()));
			}
		});
		switch (reason) {
			case BAN:
			case LEAVE:
				if (team.isInLeague()) {
					// update general alliance info for all other alliances in league
					team.getLeague().broadcast(team);
				}
				if (team.shouldDisband()) {
					PlayerAllianceService.disband(team, true);
				}
				if (reason == LeaveReson.BAN) {
					PacketSendUtility.sendPacket(leavedPlayer, SM_SYSTEM_MESSAGE.STR_FORCE_BAN_ME(banPersonName));
				}
				break;
			case LEAVE_TIMEOUT:
				if (team.shouldDisband()) {
					PlayerAllianceService.disband(team, true);
				}
				break;
			case DISBAND:
				PacketSendUtility.sendPacket(leavedPlayer, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED());
				break;
		}

		if (leavedPlayer.isOnline()) {
			PacketSendUtility.sendPacket(leavedPlayer, new SM_LEAVE_GROUP_MEMBER());
			if (team.equals(leavedPlayer.getPosition().getWorldMapInstance().getRegisteredTeam())) {
				PacketSendUtility.sendPacket(leavedPlayer, SM_SYSTEM_MESSAGE.STR_MSG_LEAVE_INSTANCE_NOT_PARTY());
				leavedPlayer.getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
					if (leavedPlayer.getCurrentTeamId() != team.getObjectId()) {
						if (leavedPlayer.getPosition().getWorldMapInstance().getRegisteredTeam() != null)
							InstanceService.moveToExitPoint(leavedPlayer);
					}
				}, 30000));
			}
		}
		super.handleEvent();
	}

}
