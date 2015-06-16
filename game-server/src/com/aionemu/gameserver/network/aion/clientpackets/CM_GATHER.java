package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ATracer
 */
public class CM_GATHER extends AionClientPacket {

	boolean isStartGather = false;

	public CM_GATHER(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		int action = readD();
		if (action == 0)
			isStartGather = true;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		VisibleObject target = player.getTarget();
		if (target != null && target.getPosition().isSpawned() && target instanceof Gatherable)
			if (isStartGather)
				((Gatherable) target).getController().onStartUse(player);
			else
				((Gatherable) target).getController().finishGathering(player);
	}
}
