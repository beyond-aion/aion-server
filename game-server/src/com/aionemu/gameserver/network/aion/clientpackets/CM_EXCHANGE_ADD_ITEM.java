package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ExchangeService;

/**
 * @author Avol
 */
public class CM_EXCHANGE_ADD_ITEM extends AionClientPacket {

	public int itemObjId;
	public int itemCount;

	public CM_EXCHANGE_ADD_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		itemObjId = readD();
		itemCount = readD();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		ExchangeService.getInstance().addItem(activePlayer, itemObjId, itemCount);
	}
}
