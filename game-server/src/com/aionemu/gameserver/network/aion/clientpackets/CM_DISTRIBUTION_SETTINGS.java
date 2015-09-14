package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team2.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.model.team2.league.LeagueService;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author Lyahim, Simple, xTz
 */
public class CM_DISTRIBUTION_SETTINGS extends AionClientPacket {

	@SuppressWarnings("unused")
	private int isLeague;
	private int lootrul;
	private int misc;
	private LootRuleType lootrules;
	private int common_item_above;
	private int superior_item_above;
	private int heroic_item_above;
	private int fabled_item_above;
	private int ethernal_item_above;
	private int mythic_item_above;
	@SuppressWarnings("unused")
	private int unk;

	public CM_DISTRIBUTION_SETTINGS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		isLeague = readD();
		lootrul = readD();
		switch (lootrul) {
			case 0:
				lootrules = LootRuleType.FREEFORALL;
				break;
			case 1:
				lootrules = LootRuleType.ROUNDROBIN;
				break;
			case 2:
				lootrules = LootRuleType.LEADER;
				break;
			default:
				lootrules = LootRuleType.FREEFORALL;
				break;
		}
		misc = readD();
		common_item_above = readD();
		superior_item_above = readD();
		heroic_item_above = readD();
		fabled_item_above = readD();
		ethernal_item_above = readD();
		mythic_item_above = readD();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player leader = getConnection().getActivePlayer();

		PlayerGroup group = leader.getPlayerGroup2();
		if (group != null) {
			PlayerGroupService.changeGroupRules(group, new LootGroupRules(lootrules, misc, common_item_above, superior_item_above, heroic_item_above,
				fabled_item_above, ethernal_item_above, mythic_item_above));
		}
		PlayerAlliance alliance = leader.getPlayerAlliance2();
		if (alliance != null) {
			if (alliance.isInLeague())
				LeagueService.changeGroupRules(alliance.getLeague(), new LootGroupRules(lootrules, misc, common_item_above, superior_item_above,
					heroic_item_above, fabled_item_above, ethernal_item_above, mythic_item_above));
			else
				PlayerAllianceService.changeGroupRules(alliance, new LootGroupRules(lootrules, misc, common_item_above, superior_item_above,
					heroic_item_above, fabled_item_above, ethernal_item_above, mythic_item_above));
		}
	}

}
