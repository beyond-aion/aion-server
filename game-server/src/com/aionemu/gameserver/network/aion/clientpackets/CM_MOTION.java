package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author MrPoke
 */
public class CM_MOTION extends AionClientPacket {

	/** The only request the client sends, other values are ignored. */
	private static final int ACTIVATE = 4;

	private int request;
	private int motionId;
	private MotionType motionType;

	public CM_MOTION(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		request = readUC();
		motionId = readUH();
		motionType = MotionType.getById(readUC());
	}

	@Override
	protected void runImpl() {
		if (request != ACTIVATE || motionType == null)
			return;
		Player player = getConnection().getActivePlayer();
		player.getMotions().setActive(motionId, motionType);
	}
}
