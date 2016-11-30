package com.aionemu.gameserver.network.loginserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * This packet is response for SM_GS_AUTH its notify Gameserver if registration was ok or what was wrong.
 * 
 * @author -Nemesiss-, Neon
 */
public class CM_GS_AUTH_RESPONSE extends LsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_GS_AUTH_RESPONSE.class);
	private int response;
	private int serverCount;

	public CM_GS_AUTH_RESPONSE(int opCode) {
		super(opCode);
	}

	@Override
	public void readImpl() {
		response = readUC();
		if (response == 0)
			serverCount = readUC();
	}

	@Override
	public void runImpl() {
		switch (response) {
			case 0: // Authed
				getConnection().setState(State.AUTHED);
				LoginServer.getInstance().setGameServerCount(serverCount);
				LoginServer.getInstance().sendLoggedInAccounts();
				break;
			case 1: // Not authed
				log.error("GameServer is not authenticated at LoginServer side!");
				getConnection().close();
				break;
			case 2: // Already registered
				log.info("GameServer is already registered at LoginServer side!");
				getConnection().close();
				break;
		}
	}
}
