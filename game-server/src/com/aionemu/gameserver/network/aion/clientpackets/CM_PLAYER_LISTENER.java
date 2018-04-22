package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.reward.WebRewardService;

/**
 * This packet is sent every five minutes by the client.
 * 
 * @author ginho1
 */
public class CM_PLAYER_LISTENER extends AionClientPacket {

	public CM_PLAYER_LISTENER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		if (GSConfig.ENABLE_WEB_REWARDS)
			WebRewardService.getInstance().sendAvailableRewards(getConnection().getActivePlayer());
	}
}
