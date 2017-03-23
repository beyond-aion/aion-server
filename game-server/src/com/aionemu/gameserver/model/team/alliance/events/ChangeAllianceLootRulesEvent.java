package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;

/**
 * @author ATracer
 */
public class ChangeAllianceLootRulesEvent extends AlwaysTrueTeamEvent {

	private final PlayerAlliance alliance;
	private final LootGroupRules lootGroupRules;

	public ChangeAllianceLootRulesEvent(PlayerAlliance alliance, LootGroupRules lootGroupRules) {
		this.alliance = alliance;
		this.lootGroupRules = lootGroupRules;
	}

	@Override
	public void handleEvent() {
		alliance.setLootGroupRules(lootGroupRules);
		alliance.sendPackets(new SM_ALLIANCE_INFO(alliance));
	}

}
