package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.team2.TeamEvent;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.google.common.base.Predicate;

/**
 *
 * @author Source
 */
public class LeagueLootRulesChangeEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember>, TeamEvent {

	private final League league;
	private final LootGroupRules lootGroupRules;

	public LeagueLootRulesChangeEvent(League league, LootGroupRules lootGroupRules) {
		this.league = league;
		this.lootGroupRules = lootGroupRules;
	}

	@Override
	public void handleEvent() {
		league.setLootGroupRules(lootGroupRules);
		league.apply(this);
	}

	@Override
	public boolean apply(LeagueMember member) {
		PlayerAlliance alliance = member.getObject();
		alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
		alliance.sendPacket(new SM_SHOW_BRAND(0, 0, true));
		return true;
	}

}