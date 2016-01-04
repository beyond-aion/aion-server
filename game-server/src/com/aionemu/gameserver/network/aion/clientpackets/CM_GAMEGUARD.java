package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.antihack.AntiHackService;

public class CM_GAMEGUARD extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_GAMEGUARD.class);
	private int size;

	public CM_GAMEGUARD(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		size = readD();
		readB(size);

	}

	@Override
	protected void runImpl() {
		log.info("AION Bin size from client: " + size);
		Player player = getConnection().getActivePlayer();
		AntiHackService.checkAionBin(size, player);
	}

}
