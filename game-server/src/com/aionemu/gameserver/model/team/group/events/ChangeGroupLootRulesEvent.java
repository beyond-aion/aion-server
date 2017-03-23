package com.aionemu.gameserver.model.team.group.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_INFO;

/**
 * @author ATracer
 */
public class ChangeGroupLootRulesEvent extends AlwaysTrueTeamEvent {

	private final PlayerGroup group;
	private final LootGroupRules lootGroupRules;

	public ChangeGroupLootRulesEvent(PlayerGroup group, LootGroupRules lootGroupRules) {
		this.group = group;
		this.lootGroupRules = lootGroupRules;
	}

	@Override
	public void handleEvent() {
		group.setLootGroupRules(lootGroupRules);
		group.sendPackets(new SM_GROUP_INFO(group));
	}

}
