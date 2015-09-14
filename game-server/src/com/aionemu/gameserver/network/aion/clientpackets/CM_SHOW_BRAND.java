package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sweetkr
 * @author Simple
 */
public class CM_SHOW_BRAND extends AionClientPacket {

	@SuppressWarnings("unused")
	private int action;
	private int brandId;
	private int targetObjectId;

	/**
	 * @param opcode
	 */
	public CM_SHOW_BRAND(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		action = readD();
		brandId = readD();
		targetObjectId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isInGroup2()) {
			if (player.getPlayerGroup2().isLeader(player)) {
				PlayerGroupService.showBrand(player, targetObjectId, brandId);
			}
		}
		// to better times (on retail still not implemented) but we have ;)
		// else if (player.isInLeague()) {
		// if (player.getPlayerAlliance2().getLeague().getLeader().getObject().isLeader(player)) {
		// LeagueService.showBrand(player, targetObjectId, brandId);
		// }
		// }
		else if (player.isInAlliance2()) {
			if (player.getPlayerAlliance2().isSomeCaptain(player)) {
				PlayerAllianceService.showBrand(player, targetObjectId, brandId);
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_SHOW_BRAND(brandId, targetObjectId));
		}
	}

}
