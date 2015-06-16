package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;

/**
 * In this packets aion client is asking if given char [by oid] may login into game [ie start playing].
 * 
 * @author -Nemesiss-, Avol
 */
public class CM_ENTER_WORLD extends AionClientPacket {

	/**
	 * Object Id of player that is entering world
	 */
	private int objectId;

	/**
	 * Constructs new instance of <tt>CM_ENTER_WORLD </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_ENTER_WORLD(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		objectId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {

		final AionConnection client = getConnection();
		PlayerEnterWorldService.startEnterWorld(objectId, client);
	}
}
