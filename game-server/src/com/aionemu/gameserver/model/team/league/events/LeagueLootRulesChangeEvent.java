package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;

/**
 * @author Source, Neon
 */
public class LeagueLootRulesChangeEvent extends AlwaysTrueTeamEvent {

	private final League league;
	private final LootGroupRules lootGroupRules;

	public LeagueLootRulesChangeEvent(League league, LootGroupRules lootGroupRules) {
		this.league = league;
		this.lootGroupRules = lootGroupRules;
	}

	@Override
	public void handleEvent() {
		league.setLootGroupRules(lootGroupRules);
		league.forEach(alliance -> alliance.sendPackets(new SM_ALLIANCE_INFO(alliance)));
	}

}
