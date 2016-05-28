package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.reward.RewardService;

/**
 * @author ginho1
 */
public class CM_PLAYER_LISTENER extends AionClientPacket {

	/*
	 * This CM is send every five minutes by client.
	 */
	public CM_PLAYER_LISTENER(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		if (GSConfig.ENABLE_WEB_REWARDS)
			RewardService.getInstance().verify(getConnection().getActivePlayer());
	}
}
