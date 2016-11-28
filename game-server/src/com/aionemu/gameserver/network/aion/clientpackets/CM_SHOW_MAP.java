package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.SerialKillerService;

/**
 * @author Lyahim
 */
public class CM_SHOW_MAP extends AionClientPacket {
	
	private int action;

	public CM_SHOW_MAP(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		switch (action) {
			case 0:
				SerialKillerService.getInstance().intruderScan(player);
				break;
		}
	}
}
