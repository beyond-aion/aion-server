package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECURITY_TOKEN;
import com.aionemu.gameserver.services.player.SecurityTokenService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ginho1
 *
 */
public class CM_SECURITY_TOKEN extends AionClientPacket {

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_SECURITY_TOKEN(int opcode, State state,
			State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		
	}

	@Override
	protected void runImpl() {
		Player player = this.getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		if (player.getPlayerAccount().getSecurityToken().equals("")) {
			SecurityTokenService.getInstance().generateToken(player);
		}
		PacketSendUtility.sendPacket(player, new SM_SECURITY_TOKEN(player.getPlayerAccount().getSecurityToken().getBytes()));
	}
}