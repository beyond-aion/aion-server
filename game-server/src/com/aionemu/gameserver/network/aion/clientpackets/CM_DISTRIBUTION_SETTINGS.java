package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.team.league.LeagueService;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author Lyahim, Simple, xTz
 */
public class CM_DISTRIBUTION_SETTINGS extends AionClientPacket {

	@SuppressWarnings("unused")
	private int isLeague;
	private int lootRule;
	private int misc;
	private LootRuleType lootRules;
	private int commonItemAbove;
	private int superiorItemAbove;
	private int heroicItemAbove;
	private int fabledItemAbove;
	private int ethernalItemAbove;
	private int mythicItemAbove;
	@SuppressWarnings("unused")
	private int unk;

	public CM_DISTRIBUTION_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		isLeague = readD();
		lootRule = readD();
		switch (lootRule) {
			case 0 -> lootRules = LootRuleType.FREEFORALL;
			case 1 -> lootRules = LootRuleType.ROUNDROBIN;
			case 2 -> lootRules = LootRuleType.LEADER;
			default -> lootRules = LootRuleType.FREEFORALL;
		}
		misc = readD();
		commonItemAbove = readD();
		superiorItemAbove = readD();
		heroicItemAbove = readD();
		fabledItemAbove = readD();
		ethernalItemAbove = readD();
		mythicItemAbove = readD();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player leader = getConnection().getActivePlayer();

		PlayerGroup group = leader.getPlayerGroup();
		if (group != null) {
			PlayerGroupService.changeGroupRules(group, new LootGroupRules(lootRules, misc, commonItemAbove, superiorItemAbove, heroicItemAbove,
				fabledItemAbove, ethernalItemAbove, mythicItemAbove));
		}
		PlayerAlliance alliance = leader.getPlayerAlliance();
		if (alliance != null) {
			if (alliance.isInLeague())
				LeagueService.changeGroupRules(alliance.getLeague(), new LootGroupRules(lootRules, misc, commonItemAbove, superiorItemAbove, heroicItemAbove,
					fabledItemAbove, ethernalItemAbove, mythicItemAbove));
			else
				PlayerAllianceService.changeGroupRules(alliance, new LootGroupRules(lootRules, misc, commonItemAbove, superiorItemAbove, heroicItemAbove,
					fabledItemAbove, ethernalItemAbove, mythicItemAbove));
		}
	}

}
