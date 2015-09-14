package com.aionemu.gameserver.network.aion.clientpackets;

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

	float x, y, z;
	int distance;
	@SuppressWarnings("unused")
	private byte locationId;
	@SuppressWarnings("unused")
	private int worldId;

	/**
	 * Constructs new instance of <tt>CM_MOVE_IN_AIR </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_MOVE_IN_AIR(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		worldId = readD();
		x = readF();
		y = readF();
		z = readF();
		locationId = (byte) readC();
		distance = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isInState(CreatureState.FLIGHT_TELEPORT)) {
			if (player.isUsingFlyTeleport()) {
				player.setFlightDistance(distance);
			} else if (player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
				player.windstreamPath.distance = distance;
			}
			World.getInstance().updatePosition(player, x, y, z, (byte) 0);
			player.getMoveController().updateLastMove();
		}
	}
}
