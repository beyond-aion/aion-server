package com.aionemu.gameserver.model.team.common.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class ShowBrandEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> extends AlwaysTrueTeamEvent implements Predicate<Player> {

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
		team.applyOnMembers(this);
	}

	@Override
	public boolean apply(Player member) {
		PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(brandId, targetObjId));
		return true;
	}

}
