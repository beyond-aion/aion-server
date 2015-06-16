package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.SecurityTokenService;

/**
 * @author Artur
 *
 */
public class CM_SECURITY_TOKEN_REQUEST extends AionClientPacket {

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_SECURITY_TOKEN_REQUEST(int opcode, State state,
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
		SecurityTokenService.getInstance().sendToken(player, player.getPlayerAccount().getSecurityToken());
		
	}

}
