package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * Sent by the client when the teleport animation is done and the actual teleport should be executed.
 * 
 * @author Rolandas, Neon
 */
public class CM_TELEPORT_ANIMATION_DONE extends AionClientPacket {

	public CM_TELEPORT_ANIMATION_DONE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Future<?> task = player.getController().getAndRemoveTask(TaskId.TELEPORT);
		if (task instanceof RunnableFuture && !task.isDone())
			try {
				RunnableFuture<?> spawnTask = (RunnableFuture<?>) task;
				spawnTask.run(); // run now since it's not started yet
				spawnTask.get(); // get to throw exception, if any
			} catch (InterruptedException | ExecutionException e) {
				LoggerFactory.getLogger(CM_TELEPORT_ANIMATION_DONE.class).error("", e.getCause());
				if (!player.isSpawned()) {
					PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
					World.getInstance().spawn(player);
				}
			}
	}

}
