package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author ATracer
 */
public class ShowBrandEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> extends AlwaysTrueTeamEvent {

	private final T team;
	private final int targetObjId;
	private final int brandId;

	public ShowBrandEvent(T team, int targetObjId, int brandId) {
		this.team = team;
		this.targetObjId = targetObjId;
		this.brandId = brandId;
	}

	@Override
	public void handleEvent() {
		team.sendPackets(new SM_SHOW_BRAND(brandId, targetObjId));
	}

}
