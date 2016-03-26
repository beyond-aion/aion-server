package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TeamEvent;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author Source
 */
public class LeagueKinahDistributionEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember>, TeamEvent {

	private final long amount;
	private final Player eventPlayer;
	private long rewardPerPlayer;
	private long membersOnline;

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

		League league = eventPlayer.getPlayerAlliance2().getLeague();
		for (LeagueMember member : league.getSortedMembers()) {
			PlayerAlliance alliance = member.getObject();
			membersOnline += alliance.onlineMembers();
		}
		if (membersOnline <= amount) {
			rewardPerPlayer = amount / membersOnline;
			if (eventPlayer.getInventory().tryDecreaseKinah(amount)) {
				league.apply(this);
			}
		}
	}

	@Override
	public boolean apply(LeagueMember member) {
		PlayerAlliance alliance = member.getObject();
		alliance.applyOnMembers(new Predicate<Player>() {

			@Override
			public boolean apply(Player member) {
				if (member.isOnline()) {
					member.getInventory().increaseKinah(rewardPerPlayer);
					if (member.equals(eventPlayer)) {
						PacketSendUtility.sendPacket(member, new SM_SYSTEM_MESSAGE(1390247, amount, membersOnline, rewardPerPlayer));
					} else {
						PacketSendUtility.sendPacket(member, new SM_SYSTEM_MESSAGE(1390248, eventPlayer.getName(), amount, membersOnline, rewardPerPlayer));
					}
				}
				return true;
			}

		});
		return true;
	}

}
