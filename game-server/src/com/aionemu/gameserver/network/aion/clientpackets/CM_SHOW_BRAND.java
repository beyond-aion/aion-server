package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sweetkr, Simple
 */
public class CM_SHOW_BRAND extends AionClientPacket {

	@SuppressWarnings("unused")
	private int action;
	private int brandId;
	private int targetObjectId;

	public CM_SHOW_BRAND(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readD();
		brandId = readD();
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isInGroup()) {
			if (player.getPlayerGroup().isLeader(player)) {
				PlayerGroupService.showBrand(player, targetObjectId, brandId);
			}
		}
		// to better times (on retail still not implemented) but we have ;)
		// else if (player.isInLeague()) {
		// if (player.getPlayerAlliance().getLeague().getLeader().getObject().isLeader(player)) {
		// LeagueService.showBrand(player, targetObjectId, brandId);
		// }
		// }
		else if (player.isInAlliance()) {
			if (player.getPlayerAlliance().isSomeCaptain(player)) {
				PlayerAllianceService.showBrand(player, targetObjectId, brandId);
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_SHOW_BRAND(brandId, targetObjectId));
		}
	}

}
