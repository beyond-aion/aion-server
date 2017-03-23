package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.ChangeLeaderEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
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
		int obj = eventPlayer.getPlayerAlliance().getObjectId();
		final LeagueMember leagueMember = league.getMember(obj);
		final int position = leagueMember.getLeaguePosition();
		leagueMember.setLeaguePosition(0);
		league.changeLeader(leagueMember);
		league.getMember(team.getObjectId()).setLeaguePosition(position);
		league.forEach(alliance -> alliance.forEach(member -> {
			PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(member.getPlayerAlliance()));
			if (team.equals(leagueMember.getObject())) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(position));

			}
			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(player.getName(), leagueMember.getLeaguePosition()));
			if (team.getLeaderObject().equals(member)) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_LEADER(player.getName(), player.getName()));
			}
		}));
	}

	@Override
	public void handleEvent() {
		if (!eventPlayer.isInLeague() || !eventPlayer.getPlayerAlliance().getLeaderObject().equals(eventPlayer)) {
			return;
		}

		changeLeaderTo(eventPlayer);
	}

}
