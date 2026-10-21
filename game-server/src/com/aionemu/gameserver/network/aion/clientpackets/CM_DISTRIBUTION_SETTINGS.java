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
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Lyahim, Simple, xTz
 */
public class CM_DISTRIBUTION_SETTINGS extends AionClientPacket {

	private boolean autoDistributeJunk;
	private LootRuleType lootRules;
	private int commonItemAbove;
	private int superiorItemAbove;
	private int heroicItemAbove;
	private int fabledItemAbove;
	private int ethernalItemAbove;
	private int mythicItemAbove;

	public CM_DISTRIBUTION_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readD(); // isLeague, the team is taken from the leader instead
		lootRules = LootRuleType.getById(readD());
		autoDistributeJunk = readD() == 1;
		commonItemAbove = readD();
		superiorItemAbove = readD();
		heroicItemAbove = readD();
		fabledItemAbove = readD();
		ethernalItemAbove = readD();
		mythicItemAbove = readD();
		readD(); // rule for a seventh item grade, which 4.8 doesn't have
	}

	@Override
	protected void runImpl() {
		Player leader = getConnection().getActivePlayer();
		if (lootRules == null)
			return;

		PlayerGroup group = leader.getPlayerGroup();
		PlayerAlliance alliance = leader.getPlayerAlliance();
		boolean isLeader = group != null && group.isLeader(leader)
			|| alliance != null && (alliance.isInLeague() ? leader.equals(alliance.getLeague().getCaptain()) : alliance.isLeader(leader));
		if (!isLeader) {
			AuditLogger.log(leader, "tried to change the loot rules without being the team leader");
			return;
		}
		if (group != null) {
			PlayerGroupService.changeGroupRules(group, new LootGroupRules(lootRules, autoDistributeJunk, commonItemAbove, superiorItemAbove, heroicItemAbove,
				fabledItemAbove, ethernalItemAbove, mythicItemAbove));
		}
		if (alliance != null) {
			if (alliance.isInLeague())
				LeagueService.changeGroupRules(alliance.getLeague(), new LootGroupRules(lootRules, autoDistributeJunk, commonItemAbove, superiorItemAbove, heroicItemAbove,
					fabledItemAbove, ethernalItemAbove, mythicItemAbove));
			else
				PlayerAllianceService.changeGroupRules(alliance, new LootGroupRules(lootRules, autoDistributeJunk, commonItemAbove, superiorItemAbove, heroicItemAbove,
					fabledItemAbove, ethernalItemAbove, mythicItemAbove));
		}
	}

}
