package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.PrivateStoreService;

/**
 * @author Simple
 */
public class CM_PRIVATE_STORE_NAME extends AionClientPacket {

	private String name;

	/**
	 * Constructs new instance of <tt>CM_PRIVATE_STORE</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_PRIVATE_STORE_NAME(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		name = readS();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		PrivateStoreService.openPrivateStore(activePlayer, name);
	}
}
