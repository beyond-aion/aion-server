package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author ATracer
 */
public class PlayerAllianceUpdateEvent extends AlwaysTrueTeamEvent {

	private final PlayerAlliance alliance;
	private final Player player;
	private final PlayerAllianceEvent allianceEvent;
	private final PlayerAllianceMember updateMember;
	private final int slot;

	public PlayerAllianceUpdateEvent(PlayerAlliance alliance, Player player, PlayerAllianceEvent allianceEvent, int slot) {
		this.alliance = alliance;
		this.player = player;
		this.allianceEvent = allianceEvent;
		this.updateMember = alliance.getMember(player.getObjectId());
		this.slot = slot;
	}

	public PlayerAllianceUpdateEvent(PlayerAlliance alliance, Player player, PlayerAllianceEvent allianceEvent) {
		this(alliance, player, allianceEvent, 0);
	}

	@Override
	public void handleEvent() {
		switch (allianceEvent) {
			case MOVEMENT:
			case UPDATE:
			case UPDATE_EFFECTS:
				alliance.sendPacket(Predicates.Players.allExcept(player), new SM_ALLIANCE_MEMBER_INFO(updateMember, allianceEvent, slot));
				break;
			default:
				// Unsupported
				break;
		}

	}

}
