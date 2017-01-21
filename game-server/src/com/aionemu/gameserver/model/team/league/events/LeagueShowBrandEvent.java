package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.google.common.base.Predicate;

/**
 * @author Tibald
 */
public class LeagueShowBrandEvent implements Predicate<LeagueMember>, TeamEvent {

	private final League league;
	private final int targetObjId;
	private final int brandId;

	public LeagueShowBrandEvent(League league, int targetObjId, int brandId) {
		this.league = league;
		this.targetObjId = targetObjId;
		this.brandId = brandId;
	}

	@Override
	public void handleEvent() {
		league.apply(this);
	}

	@Override
	public boolean apply(LeagueMember alliance) {
		alliance.getObject().sendPacket(new SM_SHOW_BRAND(brandId, targetObjId, true));
		return true;
	}

	@Override
	public boolean checkCondition() {
		return true;
	}

}
