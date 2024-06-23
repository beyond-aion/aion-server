package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Source
 */
public class LeagueMoveEvent extends AlwaysTrueTeamEvent {

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
		league.forEach(alliance -> {
			alliance.sendPackets(new SM_ALLIANCE_INFO(alliance));

			if (alliance.getObjectId() == selectedAllianceId) {
				alliance.sendPackets(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(targetCurrentPosition));
			} else {
				alliance.sendPackets(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(selectedName, targetCurrentPosition));
			}

			if (alliance.getObjectId() == targetAllianceId) {
				alliance.sendPackets(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_ME(selectedCurrentPosition));
			} else {
				alliance.sendPackets(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_FORCE_NUMBER_HIM(targetName, selectedCurrentPosition));
			}
		});
	}

}
