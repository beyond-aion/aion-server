package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;

/**
 * In this packets aion client is asking if given char [by oid] may login into game [ie start playing].
 * 
 * @author -Nemesiss-, Avol, Neon
 */
public class CM_ENTER_WORLD extends AionClientPacket {

	/**
	 * Object Id of player that is entering world
	 */
	private int objectId;

	public CM_ENTER_WORLD(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
	}

	@Override
	protected void runImpl() {
		PlayerEnterWorldService.enterWorld(getConnection(), objectId);
	}
}
