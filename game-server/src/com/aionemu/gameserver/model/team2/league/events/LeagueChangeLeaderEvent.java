package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.ChangeLeaderEvent;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 *
 * @author xTz
 */
public class LeagueChangeLeaderEvent extends ChangeLeaderEvent<PlayerAlliance> {

	private League league;

	public LeagueChangeLeaderEvent(PlayerAlliance team, Player eventPlayer) {
		super(team, eventPlayer);
		league = team.getLeague();
	}

	@Override
	protected void changeLeaderTo(final Player player) {
		Integer obj = eventPlayer.getPlayerAlliance2().getObjectId();
		final LeagueMember leagueMember = league.getMember(obj);
		final int position = leagueMember.getLeaguePosition();
		leagueMember.setLeaguePosition(0);
		league.changeLeader(leagueMember);
		league.getMember(team.getObjectId()).setLeaguePosition(position);
		league.apply(new Predicate<LeagueMember>() {
			@Override
			public boolean apply(final LeagueMember leagueMember) {
				leagueMember.getObject().applyOnMembers(new Predicate<Player>() {
					
					@Override
					public boolean apply(Player member) {
						PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(member.getPlayerAlliance2()));
						if (team.equals(leagueMember.getObject())) {
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(position));
						
						}
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(player.getName(), leagueMember.getLeaguePosition()));
						if (team.getLeaderObject().equals(member)) {
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_LEADER(player.getName(), player.getName()));
						}
	
						return true;
					}

				});
				return true;
			}

		});
	}

	@Override
	public void handleEvent() {
		if (!eventPlayer.isInLeague() || !eventPlayer.getPlayerAlliance2().getLeaderObject().equals(eventPlayer)) {
			return;
		}
	
		changeLeaderTo(eventPlayer);
	}

}
