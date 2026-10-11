package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke
 */
public class SM_MOTION extends AionServerPacket {

	private enum Action {
		LIST(1),
		ADD(2),
		SET(5),
		REMOVE(6),
		PLAYER_MOTIONS(7);

		private final int id;

		Action(int id) {
			this.id = id;
		}
	}

	private final Action action;
	private final int motionId;
	private final int remainingTime;
	private final MotionType type;
	private final int playerId;
	private final Map<MotionType, Motion> activeMotions;
	private final Collection<Motion> motions;

	private SM_MOTION(Action action, int motionId, int remainingTime, MotionType type, int playerId, Map<MotionType, Motion> activeMotions,
		Collection<Motion> motions) {
		this.action = action;
		this.motionId = motionId;
		this.remainingTime = remainingTime;
		this.type = type;
		this.playerId = playerId;
		this.activeMotions = activeMotions;
		this.motions = motions;
	}

	/**
	 * All learned motions of the player.
	 */
	public static SM_MOTION list(Collection<Motion> motions) {
		return new SM_MOTION(Action.LIST, 0, 0, null, 0, null, motions);
	}

	/**
	 * A newly learned motion.
	 */
	public static SM_MOTION add(Motion motion) {
		return new SM_MOTION(Action.ADD, motion.getId(), motion.secondsUntilExpiration(), null, 0, null, null);
	}

	/**
	 * Answer to activating a motion, or to clearing the slot if motionId is 0.
	 */
	public static SM_MOTION set(int motionId, MotionType type) {
		return new SM_MOTION(Action.SET, motionId, 0, type, 0, null, null);
	}

	/**
	 * An expired motion, the client prints the expiry message itself.
	 */
	public static SM_MOTION remove(int motionId) {
		return new SM_MOTION(Action.REMOVE, motionId, 0, null, 0, null, null);
	}

	/**
	 * The active motion of each slot of the player, for everyone who sees him.
	 */
	public static SM_MOTION playerMotions(Player player) {
		return new SM_MOTION(Action.PLAYER_MOTIONS, 0, 0, null, player.getObjectId(), player.getMotions().getActiveMotions(), null);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action.id);
		switch (action) {
			case LIST -> {
				writeH(motions.size());
				for (Motion motion : motions) {
					writeH(motion.getId());
					writeD(motion.secondsUntilExpiration());
					writeC(motion.isActive() ? 1 : 0);
				}
			}
			case ADD -> {
				writeH(motionId);
				writeD(remainingTime);
			}
			case SET -> {
				writeH(motionId);
				writeC(type.getId());
			}
			case REMOVE -> writeH(motionId);
			case PLAYER_MOTIONS -> {
				writeD(playerId);
				for (MotionType motionType : MotionType.values()) {
					Motion motion = activeMotions.get(motionType);
					writeH(motion == null ? 0 : motion.getId());
				}
			}
		}
	}
}
