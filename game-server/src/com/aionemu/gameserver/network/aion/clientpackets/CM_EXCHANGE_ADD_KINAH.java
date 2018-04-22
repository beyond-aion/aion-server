package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ExchangeService;

/**
 * @author Avol
 */
public class CM_EXCHANGE_ADD_KINAH extends AionClientPacket {

	private long kinahCount;

	public CM_EXCHANGE_ADD_KINAH(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		kinahCount = readQ();
	}

	@Override
	protected void runImpl() {
		ExchangeService.getInstance().addKinah(getConnection().getActivePlayer(), kinahCount);
	}
}
