package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;

/**
 * In this packets aion client is asking if may quit.
 * 
 * @author -Nemesiss-
 */
public class CM_QUIT extends AionClientPacket {

	/**
	 * Logout - if true player is wanted to go to character selection.
	 */
	private boolean logout;

	/**
	 * Constructs new instance of <tt>CM_QUIT </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_QUIT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		logout = readC() == 1;
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();

		Player player = null;
		if (client.getState() == State.IN_GAME) {
			player = client.getActivePlayer();
			// TODO! check if may quit
			if (!logout)
				LoginServer.getInstance().aionClientDisconnected(client.getAccount().getId());

			PlayerLeaveWorldService.startLeaveWorld(player);
			client.setActivePlayer(null);
		}

		if (logout) {
			if (player != null && player.isInEditMode()) {
				sendPacket(new SM_QUIT_RESPONSE(true));
				player.setEditMode(false);
			}
			else
				sendPacket(new SM_QUIT_RESPONSE());
		}
		else {
			client.close(new SM_QUIT_RESPONSE(), false);
		}
	}
}
