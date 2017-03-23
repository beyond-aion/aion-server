package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
public class LeagueKinahDistributionEvent extends AlwaysTrueTeamEvent {

	private final long amount;
	private final Player eventPlayer;
	private long rewardPerPlayer;
	private int membersOnline;

	public LeagueKinahDistributionEvent(Player player, long amount) {
		eventPlayer = player;
		this.amount = amount;
	}

	@Override
	public void handleEvent() {
		if (eventPlayer.getInventory().getKinah() < amount) {
			PacketSendUtility.sendPacket(eventPlayer, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		League league = eventPlayer.getPlayerAlliance().getLeague();
		for (LeagueMember member : league.getSortedMembers()) {
			PlayerAlliance alliance = member.getObject();
			membersOnline += alliance.onlineMembers();
		}
		if (membersOnline <= amount) {
			rewardPerPlayer = amount / membersOnline;
			if (eventPlayer.getInventory().tryDecreaseKinah(amount)) {
				league.forEach(alliance -> {
					alliance.forEach(member -> {
						if (member.isOnline()) {
							member.getInventory().increaseKinah(rewardPerPlayer);
							if (member.equals(eventPlayer)) {
								PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_ME_TO_B(amount, membersOnline, rewardPerPlayer));
							} else {
								PacketSendUtility.sendPacket(member,
									SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_B_TO_ME(eventPlayer.getName(), amount, membersOnline, rewardPerPlayer));
							}
						}
					});
				});
			}
		}
	}

}
