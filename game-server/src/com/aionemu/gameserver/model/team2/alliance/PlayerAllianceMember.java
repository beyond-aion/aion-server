package com.aionemu.gameserver.model.team2.alliance;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.PlayerTeamMember;

/**
 * @author ATracer
 */
public class PlayerAllianceMember extends PlayerTeamMember {

	private int allianceId;

	public PlayerAllianceMember(Player player) {
		super(player);
	}

	public int getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(int allianceId) {
		this.allianceId = allianceId;
	}

	public final PlayerAllianceGroup getPlayerAllianceGroup() {
		return getObject().getPlayerAllianceGroup2();
	}

	public final void setPlayerAllianceGroup(PlayerAllianceGroup playerAllianceGroup) {
		getObject().setPlayerAllianceGroup2(playerAllianceGroup);
	}

}
