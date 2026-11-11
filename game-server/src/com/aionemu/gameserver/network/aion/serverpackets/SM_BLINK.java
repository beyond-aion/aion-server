package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.clientpackets.CM_BLINK;

/**
 * Instantly moves the player to the given position and cancels movement on client side (just like {@link SM_POSITION}). The client responds with
 * {@link CM_BLINK} afterward.
 *
 * @author cura
 */
public class SM_BLINK extends AionServerPacket {

	/**
	 * The reasons retail passes to its blink. The client reads the id right after the heading; 4, 5 and 6 additionally reset its area manager,
	 * every other one is only logged there. {@link #NONE} is not used by retail, it stands for a blink the server raises without naming a cause.
	 */
	public static enum BlinkReason {
		/** No reason named; retail always passes one. */
		NONE(0),
		GAME_BLOCK_NO_MOVE(1),
		MOVE_IN_NO_MOVE_STATE(2),
		TOO_FAST_MOVE(3),
		MOVE_DOWN_TERRAIN(4),
		MOVE_THROUGH_TERRAIN(5),
		MOVE_IN_COLLISION(6),
		IN_TELEPORT(7),
		FLY_ON_NO_FLY_AREA(8),
		IN_PATH_FLIGHT(9),
		INACTIVE_PATH(10),
		TOO_FAST_IN_PATH(11),
		ALTITUDE_ABUSING(13),
		CRAWL_CLIMB(14),
		TOO_FAST_MOVE_AFTER_WIND_PATH(15),
		MOVE_SPEED_HACK(16),
		Z_CHEATING(17),
		MOVE_IN_ESCAPE_SKILL_CASTING(18),
		MOVE_TO_PORTAL_OF_INVALID_LITE_CLIENT(19),
		MOVE_TO_PORTAL_OF_INVALID_RACE(20),
		WORLD_BLINK(21);

		private final int id;

		private BlinkReason(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	private final float x, y, z;
	private final byte heading;
	private final BlinkReason reason;

	public SM_BLINK(float x, float y, float z, byte heading) {
		this(x, y, z, heading, BlinkReason.NONE);
	}

	public SM_BLINK(float x, float y, float z, byte heading, BlinkReason reason) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.reason = reason;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);
		writeC(reason.getId());
	}
}
