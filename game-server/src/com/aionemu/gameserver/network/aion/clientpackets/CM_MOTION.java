package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author MrPoke
 */
public class CM_MOTION extends AionClientPacket {

	private int motionId;
	private int motionType;

	public CM_MOTION(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readC(); // unk 4
		motionId = readUH();
		motionType = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.getMotions().setActive(motionId, motionType);
	}
}
