package com.aionemu.gameserver.network.loginserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.gameserver.network.NetworkController;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_LIST;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_GS_AUTH;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * This packet is response for SM_GS_AUTH its notify Gameserver if registration was ok or what was wrong.
 * 
 * @author -Nemesiss-
 */
public class CM_GS_AUTH_RESPONSE extends LsClientPacket {

	public CM_GS_AUTH_RESPONSE(int opCode) {
		super(opCode);
	}

	/**
	 * Logger for this class.
	 */
	protected static final Logger log = LoggerFactory.getLogger(CM_GS_AUTH_RESPONSE.class);

	/**
	 * Response: 0=Authed,1=NotAuthed,2=AlreadyRegistered
	 */
	private int response;

	private byte serverCount;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readImpl() {
		response = readC();
		if (response == 0)
			serverCount = (byte) readC();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runImpl() {
		/**
		 * Authed
		 */
		if (response == 0) {
			getConnection().setState(State.AUTHED);
			sendPacket(new SM_ACCOUNT_LIST(LoginServer.getInstance().getLoggedInAccounts()));
			NetworkController.getInstance().setServerCount(serverCount);
		}

		/**
		 * NotAuthed
		 */
		else if (response == 1) {
			log.error("GameServer is not authenticated at LoginServer side, shutting down!");
			System.exit(ExitCode.CODE_ERROR);
		}
		/**
		 * AlreadyRegistered
		 */
		else if (response == 2) {
			log.info("GameServer is already registered at LoginServer side! trying again...");
			/**
			 * try again after 10s
			 */
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					CM_GS_AUTH_RESPONSE.this.sendPacket(new SM_GS_AUTH());
				}

			}, 10000);
		}
	}
}
