package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.task.GatheringTask;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class CM_GATHER extends AionClientPacket {

	private int actionId;

	public CM_GATHER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		actionId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.getController().cancelUseItem(); // including the stop action
		player.getController().cancelCurrentSkill(null);
		switch (actionId) {
			case -1 -> cancelGathering(player);
			case 0, 128 -> startGathering(player); // 128 is sent when using /attack chat command
			default -> LoggerFactory.getLogger(getClass()).warn("Unhandled gathering action ID {} (sent by {} at {})", actionId, player, player.getPosition());
		}
	}

	private void startGathering(Player player) {
		if (player.getTarget() instanceof Gatherable gatherable)
			gatherable.getController().startGathering(player);
		else
			AuditLogger.log(player, "tried to gather from " + player.getTarget());
	}

	private void cancelGathering(Player player) {
		if (player.getInteractionTask() instanceof GatheringTask gatheringTask)
			gatheringTask.abort();
	}
}
