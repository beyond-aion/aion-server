package com.aionemu.gameserver.model.gameobjects.player.motion;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.dao.MotionDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class MotionList {

	private final Player owner;
	private Map<MotionType, Motion> activeMotions;
	private Map<Integer, Motion> motions;

	/**
	 * @param owner
	 */
	public MotionList(Player owner) {
		this.owner = owner;
	}

	/**
	 * @return the activeMotions
	 */
	public Map<MotionType, Motion> getActiveMotions() {
		if (activeMotions == null)
			return Collections.emptyMap();
		return activeMotions;
	}

	/**
	 * @return the motions
	 */
	public Map<Integer, Motion> getMotions() {
		if (motions == null)
			return Collections.emptyMap();
		return motions;
	}

	public void add(Motion motion, boolean persist) {
		if (motions == null)
			motions = new LinkedHashMap<>();
		if (motions.containsKey(motion.getId()) && motion.getExpireTime() == 0) {
			remove(motion.getId());
		}
		motions.put(motion.getId(), motion);
		if (motion.isActive() && motion.getType() != null) {
			if (activeMotions == null)
				activeMotions = new EnumMap<>(MotionType.class);
			Motion old = activeMotions.put(motion.getType(), motion);
			if (old != null) {
				old.setActive(false);
				MotionDAO.updateMotion(owner.getObjectId(), old);
			}
		}
		if (persist) {
			ExpireTimerTask.getInstance().registerExpirable(motion, owner);
			MotionDAO.storeMotion(owner.getObjectId(), motion);
		}
	}

	public boolean remove(int motionId) {
		Motion motion = motions.remove(motionId);
		if (motion != null) {
			PacketSendUtility.sendPacket(owner, SM_MOTION.remove(motionId));
			MotionDAO.deleteMotion(owner.getObjectId(), motionId);
			if (motion.isActive()) {
				if (activeMotions != null)
					activeMotions.remove(motion.getType(), motion);
				return true;
			}
		}
		return false;
	}

	/**
	 * Activates the motion in its own slot, or clears the given slot if motionId is 0.
	 */
	public void setActive(int motionId, MotionType type) {
		if (motionId != 0) {
			Motion motion = getMotions().get(motionId);
			if (motion == null || motion.getType() == null)
				return;
			if (activeMotions == null)
				activeMotions = new EnumMap<>(MotionType.class);
			Motion old = activeMotions.put(motion.getType(), motion);
			if (old != null && old != motion) {
				old.setActive(false);
				MotionDAO.updateMotion(owner.getObjectId(), old);
			}
			motion.setActive(true);
			MotionDAO.updateMotion(owner.getObjectId(), motion);
		} else if (activeMotions != null) {
			Motion old = activeMotions.remove(type);
			if (old != null) {
				old.setActive(false);
				MotionDAO.updateMotion(owner.getObjectId(), old);
			}
		}
		PacketSendUtility.sendPacket(owner, SM_MOTION.set(motionId, type));
		PacketSendUtility.broadcastPacket(owner, SM_MOTION.playerMotions(owner), true);
	}
}
