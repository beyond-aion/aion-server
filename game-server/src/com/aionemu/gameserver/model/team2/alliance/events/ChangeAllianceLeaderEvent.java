package com.aionemu.gameserver.model.team2.alliance.events;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.alliance.events.AssignViceCaptainEvent.AssignType;
import com.aionemu.gameserver.model.team2.common.events.ChangeLeaderEvent;
import com.aionemu.gameserver.model.team2.group.PlayerFilters;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ATracer
 */
public class ChangeAllianceLeaderEvent extends ChangeLeaderEvent<PlayerAlliance> {

	public ChangeAllianceLeaderEvent(PlayerAlliance team, Player eventPlayer) {
		super(team, eventPlayer);
	}

	public ChangeAllianceLeaderEvent(PlayerAlliance team) {
		super(team, null);
	}

	@Override
	public void handleEvent() {
		Player oldLeader = team.getLeaderObject();
		if (eventPlayer == null) {
			Collection<Integer> viceCaptainIds = team.getViceCaptainIds();
			for (Integer viceCaptainId : viceCaptainIds) {
				PlayerAllianceMember viceCaptain = team.getMember(viceCaptainId);
				if (viceCaptain.isOnline()) {
					changeLeaderTo(viceCaptain.getObject());
					viceCaptainIds.remove(viceCaptainId);
					break;
				}
			}
			if (team.isLeader(oldLeader)) {
				team.applyOnMembers(this);
			}
		}
		else {
			changeLeaderTo(eventPlayer);
		}
		if (checkLeaderChanged(oldLeader)) {
			if (team.isInLeague()) {
				team.getLeague().broadcast(team);
			}
		}
		if (eventPlayer != null) {
			PlayerAllianceService.changeViceCaptain(oldLeader, AssignType.DEMOTE_CAPTAIN_TO_VICECAPTAIN);
		}
	}

	@Override
	protected void changeLeaderTo(final Player player) {
		final boolean inLeague = team.isInLeague();
		team.changeLeader(team.getMember(player.getObjectId()));
		if (inLeague) {
			team.getLeague().broadcast();
		}
		team.applyOnMembers(new Predicate<Player>() {

			@Override
			public boolean apply(Player member) {
				if (!inLeague) {
					PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(team));
					PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(0, 0, false));
				}
				if (!player.equals(member)) {
					//eventPlayer null only when leader leave by own will and wee not must inform him about new leader
					if (eventPlayer != null) {
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_FORCE_HE_IS_NEW_LEADER(player.getName()));
					}
					if (inLeague && team.getLeague().getCaptain().equals(player)) {
						team.getLeague().applyOnMembers(new Predicate<PlayerAlliance>() {

							@Override
							public boolean apply(PlayerAlliance alliance) {
								alliance.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_LEADER_TIMEOUT(player.getName(), StringUtils.EMPTY, StringUtils.EMPTY),
													new PlayerFilters.ExcludePlayerFilter(player));
								return true;
							}

						});
					}
				}
				else {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_FORCE_YOU_BECOME_NEW_LEADER);
					if (inLeague && team.getLeague().getCaptain().equals(player)) {
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_YOU_BECOME_NEW_LEADER_TIMEOUT);
					}
				}
				return true;
			}

		});
	}

}