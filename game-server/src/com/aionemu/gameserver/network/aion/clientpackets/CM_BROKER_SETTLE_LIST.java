package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.BrokerService;

/**
 * @author kosyachok
 */
public class CM_BROKER_SETTLE_LIST extends AionClientPacket {

	@SuppressWarnings("unused")
	private int npcId;

	public CM_BROKER_SETTLE_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		npcId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		BrokerService.getInstance().showSettledItems(player);
	}
}
