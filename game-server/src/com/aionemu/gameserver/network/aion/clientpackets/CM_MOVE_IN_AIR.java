package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flypath.FlightPath;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;

/**
 * Packet about player flying teleport movement.
 * 
 * @author -Nemesiss-, Sweetkr, KID
 */
public class CM_MOVE_IN_AIR extends AionClientPacket {

	/**
	 * Allowed distance between the reported position and the position on the windstream path at the reported progress.
	 */
	private static final float MAX_WINDSTREAM_DEVIATION = 170;

	private int worldId;
	private float x, y, z;
	private byte heading;
	private int progress;

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
		progress = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isSpawned())
			return;
		if (!player.isInState(CreatureState.FLYING) || worldId != player.getWorldId())
			return;

		if (player.getFlightPath() != null) {
			if (!isOnWindstreamPath(player.getFlightPath()))
				return;
			player.getFlightPath().setDistance(progress);
		}

		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		World.getInstance().updatePosition(player, x, y, z, heading);
		player.getMoveController().onMoveFromClient();
		player.getController().onMove();
	}

	private boolean isOnWindstreamPath(FlightPath flightPath) {
		if (flightPath.getType() != FlightPath.Type.WINDSTREAM)
			return true;
		FlyPathEntry path = DataManager.FLY_PATH.getPathTemplate(flightPath.getId() / 1000);
		if (path == null || !path.isWindstream())
			return true;
		if (!flightPath.isProgressReachable(progress))
			return false;
		Point3D position = path.getWindstreamPosition(progress + flightPath.getBoostGain(progress));
		return PositionUtil.isInRange(x, y, z, position.getX(), position.getY(), position.getZ(), MAX_WINDSTREAM_DEVIATION);
	}
}
