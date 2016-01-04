package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.SecurityTokenService;

/**
 * @author Artur
 */
public class CM_SECURITY_TOKEN_REQUEST extends AionClientPacket {

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_SECURITY_TOKEN_REQUEST(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {

	}

	@Override
	protected void runImpl() {
		Account account = this.getConnection().getAccount();
		if (account == null)
			return;
		if (account.getSecurityToken().equals(""))
			SecurityTokenService.getInstance().generateToken(account, this.getConnection());
		SecurityTokenService.getInstance().sendToken(this.getConnection(), account.getSecurityToken());
	}
}
