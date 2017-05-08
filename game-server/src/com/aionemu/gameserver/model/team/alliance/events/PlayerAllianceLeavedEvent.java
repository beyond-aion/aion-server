package com.aionemu.gameserver.model.team.alliance.events;

import java.util.Objects;

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
		Player leader = team.getLeaderObject();
		Objects.requireNonNull(leader, "Alliance leader should not be null");

		team.getViceCaptainIds().remove(leavedPlayer.getObjectId());

		if (!reason.equals(PlayerLeavedEvent.LeaveReson.DISBAND)) {
			// here we already must have a leader of the team
			if (leavedPlayer.equals(leader)) {
				team.onEvent(new ChangeAllianceLeaderEvent(team));
			}
		}

		if (leavedPlayer.isOnline()) {
			PacketSendUtility.sendPacket(leavedPlayer, new SM_LEAVE_GROUP_MEMBER());
		}

		team.removeMember(leavedPlayer.getObjectId());

		team.forEach(player -> {
			switch (reason) {
				case BAN:
				case LEAVE:
					PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(leavedTeamMember, PlayerAllianceEvent.LEAVE)); // LEAVE/BANNED have same id
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_LEAVE_HIM(leavedPlayer.getName()));
					PacketSendUtility.sendPacket(player, new SM_ALLIANCE_INFO(team));
					PacketSendUtility.sendPacket(player, new SM_SHOW_BRAND(0, 0, team.isInLeague()));
					break;
				case LEAVE_TIMEOUT:
					PacketSendUtility.sendPacket(player, new SM_ALLIANCE_MEMBER_INFO(leavedTeamMember, PlayerAllianceEvent.LEAVE_TIMEOUT));
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_LEAVED_PARTY_OFFLINE_TIMEOUT(leavedPlayer.getName()));
					break;
				case DISBAND:
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED());
					break;
			}
		});
		switch (reason) {
			case BAN:
			case LEAVE:
				if (team.isInLeague()) {
					// broadcast to all in league (some alliance has -1 member count)
					team.getLeague().broadcast();
				}
				if (team.size() <= 1) {
					PlayerAllianceService.disband(team, true);
				}
				if (reason == PlayerLeavedEvent.LeaveReson.BAN) {
					PacketSendUtility.sendPacket(leavedPlayer, SM_SYSTEM_MESSAGE.STR_FORCE_BAN_ME(banPersonName));
				}
				break;
			case LEAVE_TIMEOUT:
				if (team.size() <= 1) {
					PlayerAllianceService.disband(team, true);
				}
				break;
			case DISBAND:
				PacketSendUtility.sendPacket(leavedPlayer, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED());
				break;
		}

		if (leavedPlayer.isInInstance()) {
			leavedPlayer.getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (leavedPlayer.getCurrentTeamId() != team.getObjectId()) {
						if (leavedPlayer.getPosition().getWorldMapInstance().getRegisteredTeam() != null)
							InstanceService.moveToExitPoint(leavedPlayer);
					}
				}

			}, 30000));
		}
	}

}
