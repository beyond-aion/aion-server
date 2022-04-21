package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECURITY_TOKEN;
import com.aionemu.gameserver.services.player.SecurityTokenService;

/**
 * @author ginho1
 */
public class CM_SECURITY_TOKEN extends AionClientPacket {

	public CM_SECURITY_TOKEN(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {

	}

	@Override
	protected void runImpl() {
		Account account = getConnection().getAccount();
		if (account == null)
			return;
		if (account.getSecurityToken().equals(""))
			SecurityTokenService.generateToken(account);
		sendPacket(new SM_SECURITY_TOKEN(account.getSecurityToken().getBytes()));
	}
}
