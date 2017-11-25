package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke
 */
public class SM_MOTION extends AionServerPacket {

	byte action;
	short motionId;
	int remainingTime;

	int playerId;
	Map<Integer, Motion> activeMotions;

	Collection<Motion> motions;

	byte type;

	/**
	 * @param motions
	 */
	public SM_MOTION(Collection<Motion> motions) {
		this.action = 1;
		this.motions = motions;
	}

	/**
	 * @param motionId
	 * @param remainingTime
	 */
	public SM_MOTION(short motionId, int remainingTime) {
		this.action = 2;
		this.motionId = motionId;
		this.remainingTime = remainingTime;
	}

	/**
	 * @param motionId
	 * @param remainingTime
	 */
	public SM_MOTION(short motionId, byte type) {
		this.action = 5;
		this.motionId = motionId;
		this.type = type;
	}

	/**
	 * @param motionId
	 * @param remainingTime
	 */
	public SM_MOTION(short motionId) {
		this.action = 6;
		this.motionId = motionId;
	}

	/**
	 * @param playerId
	 * @param activeMotions
	 */
	public SM_MOTION(int playerId, Map<Integer, Motion> activeMotions) {
		this.action = 7;
		this.playerId = playerId;
		this.activeMotions = activeMotions;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		switch (action) {
			case 1:
				writeH(motions.size());
				for (Motion motion : motions) {
					writeH(motion.getId());
					writeD(motion.secondsUntilExpiration());
					writeC(motion.isActive() ? 1 : 0);
				}
				break;
			case 2: // Add motion
				writeH(motionId);
				writeD(remainingTime);
				break;
			case 5: // Set motion
				writeH(motionId);
				writeC(type);
				break;
			case 6: // remove
				writeH(motionId);
				break;
			case 7: // Player motions
				writeD(playerId);
				for (int i = 1; i < 6; i++) {
					Motion motion = activeMotions.get(i);
					if (motion == null)
						writeH(0);
					else
						writeH(motion.getId());
				}
		}
	}
}
