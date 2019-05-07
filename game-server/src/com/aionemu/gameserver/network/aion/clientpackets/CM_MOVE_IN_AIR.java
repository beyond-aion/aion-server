package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.world.World;

/**
 * Packet about player flying teleport movement.
 * 
 * @author -Nemesiss-, Sweetkr, KID
 */
public class CM_MOVE_IN_AIR extends AionClientPacket {

	@SuppressWarnings("unused")
	private int worldId;
	private float x, y, z;
	private byte heading;
	private int distance;

	/**
	 * Constructs new instance of <tt>CM_MOVE_IN_AIR</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_MOVE_IN_AIR(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		worldId = readD();
		x = readF();
		y = readF();
		z = readF();
		heading = readC();
		distance = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isSpawned())
			return;
		if (!player.isInState(CreatureState.FLYING))
			return;

		if (player.isUsingFlyTeleport()) {
			player.setFlightDistance(distance);
		} else if (player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
			player.windstreamPath.distance = distance;
		}

		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		World.getInstance().updatePosition(player, x, y, z, heading);
		player.getMoveController().onMoveFromClient();
	}
}
