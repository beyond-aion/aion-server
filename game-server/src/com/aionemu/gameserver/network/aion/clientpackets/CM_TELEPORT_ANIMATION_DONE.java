package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * Sent by the client when the teleport animation is done and the actual teleport should be executed.
 * 
 * @author Rolandas, Neon
 */
public class CM_TELEPORT_ANIMATION_DONE extends AionClientPacket {

	public CM_TELEPORT_ANIMATION_DONE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Future<?> task = player.getController().getTask(TaskId.RESPAWN);
		if (task instanceof RunnableFuture && !task.isDone())
			((RunnableFuture<?>) task).run();
	}

}
