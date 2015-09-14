package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;

/**
 * @author nrg
 */
public class CM_INSTANCE_INFO extends AionClientPacket {

	private static Logger log = LoggerFactory.getLogger(CM_INSTANCE_INFO.class);

	@SuppressWarnings("unused")
	private int unk1, unk2;

	public CM_INSTANCE_INFO(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		unk1 = readD();
		unk2 = readC(); // team?
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		if (unk2 == 1 && !getConnection().getActivePlayer().isInTeam())
			log.debug("Received CM_INSTANCE_INFO with teamdata request but player has no team!");
		sendPacket(new SM_INSTANCE_INFO(getConnection().getActivePlayer(), true, getConnection().getActivePlayer().getCurrentTeam()));
	}
}
