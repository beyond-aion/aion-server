package com.aionemu.gameserver.model.team2.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class ChangeAllianceLootRulesEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

	private final PlayerAlliance alliance;
	private final LootGroupRules lootGroupRules;

	public ChangeAllianceLootRulesEvent(PlayerAlliance alliance, LootGroupRules lootGroupRules) {
		this.alliance = alliance;
		this.lootGroupRules = lootGroupRules;
	}

	@Override
	public void handleEvent() {
		alliance.setLootGroupRules(lootGroupRules);
		alliance.applyOnMembers(this);
	}

	@Override
	public boolean apply(Player member) {
		PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(alliance));
		return true;
	}

}
