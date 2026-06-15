package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
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
		// player can switch targets during gathering, so the target is not guaranteed to be the correct gatherable
		Gatherable gatherable = player.getTarget() instanceof Gatherable g && g.getController().getGatheringPlayerId() == player.getObjectId() ? g : null;
		if (gatherable == null) {
			gatherable = player.getKnownList().stream()
				.filter(o -> o.get() instanceof Gatherable g && g.getController().getGatheringPlayerId() == player.getObjectId())
				.findFirst()
				.map(o -> (Gatherable) o.get())
				.orElse(null);
		}
		if (gatherable != null)
			gatherable.getController().cancelGathering();
	}
}
