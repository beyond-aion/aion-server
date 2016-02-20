package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ExchangeService;

/**
 * @author Avol
 */
public class CM_EXCHANGE_ADD_KINAH extends AionClientPacket {

	private long kinahCount;

	public CM_EXCHANGE_ADD_KINAH(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
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
