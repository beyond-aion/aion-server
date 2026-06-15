package com.aionemu.gameserver.model.team.alliance;

import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;

/**
 * @author ATracer
 */
public class PlayerAllianceGroup extends TemporaryPlayerTeam<PlayerAllianceMember> {

	private final PlayerAlliance alliance;

	public PlayerAllianceGroup(PlayerAlliance alliance, int objId) {
		super(objId, false);
		this.alliance = alliance;
	}

	@Override
	public void addMember(PlayerAllianceMember member) {
		super.addMember(member);
		member.setPlayerAllianceGroup(this);
		member.setAllianceId(getTeamId());
	}

	@Override
	public void onRemoveMember(PlayerAllianceMember member) {
		member.setPlayerAllianceGroup(null);
	}

	@Override
	public int getMaxMemberCount() {
		return 6;
	}

	@Override
	public int getMinExpPlayerLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxExpPlayerLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public PlayerAlliance getAlliance() {
		return alliance;
	}

	@Override
	public LootGroupRules getLootGroupRules() {
		return alliance.getLootGroupRules();
	}
}
