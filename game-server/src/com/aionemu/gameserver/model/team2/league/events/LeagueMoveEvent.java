package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.team2.TeamEvent;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.google.common.base.Predicate;

/**
 * @author Source
 */
public class LeagueMoveEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember>, TeamEvent {

	private final League league;
	private final int selectedAllianceId;
	private final int targetAllianceId;
	private String selectedName;
	private String targetName;
	private int selectedCurrentPosition;
	private int targetCurrentPosition;

	public LeagueMoveEvent(League league, int selectedAllianceId, int targetAllianceId) {
		this.league = league;
		this.selectedAllianceId = selectedAllianceId;
		this.targetAllianceId = targetAllianceId;
	}

	@Override
	public void handleEvent() {
		LeagueMember selected = league.getMember(selectedAllianceId);
		LeagueMember target = league.getMember(targetAllianceId);
		selectedCurrentPosition = selected.getLeaguePosition();
		targetCurrentPosition = target.getLeaguePosition();
		selected.setLeaguePosition(targetCurrentPosition);
		target.setLeaguePosition(selectedCurrentPosition);
		selectedName = selected.getObject().getLeaderObject().getName();
		targetName = target.getObject().getLeaderObject().getName();
		league.apply(this);
	}

	@Override
	public boolean apply(LeagueMember member) {
		PlayerAlliance alliance = member.getObject();
		alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
		alliance.sendPacket(new SM_SHOW_BRAND(0, 0, true));

		if (member.getObjectId() == selectedAllianceId) {
			alliance.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(targetCurrentPosition));
		} else {
			alliance.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(selectedName, targetCurrentPosition));
		}

		if (member.getObjectId() == targetAllianceId) {
			alliance.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(selectedCurrentPosition));
		} else {
			alliance.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(targetName, selectedCurrentPosition));
		}
		return true;
	}

}
