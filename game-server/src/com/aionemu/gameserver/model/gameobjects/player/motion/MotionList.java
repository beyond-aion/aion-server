package com.aionemu.gameserver.model.gameobjects.player.motion;

import java.util.Collections;
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

	private Player owner;
	private Map<Integer, Motion> activeMotions;
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
	public Map<Integer, Motion> getActiveMotions() {
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
		if (motion.isActive()) {
			if (activeMotions == null)
				activeMotions = new LinkedHashMap<>();
			Motion old = activeMotions.put(Motion.motionType.get(motion.getId()), motion);
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
			PacketSendUtility.sendPacket(owner, new SM_MOTION((short) motionId));
			MotionDAO.deleteMotion(owner.getObjectId(), motionId);
			if (motion.isActive()) {
				activeMotions.remove(Motion.motionType.get(motionId));
				return true;
			}
		}
		return false;
	}

	public void setActive(int motionId, int motionType) {
		if (motionId != 0) {
			Motion motion = motions.get(motionId);
			if (motion == null || motion.isActive())
				return;
			if (activeMotions == null)
				activeMotions = new LinkedHashMap<>();
			Motion old = activeMotions.put(motionType, motion);
			if (old != null) {
				old.setActive(false);
				MotionDAO.updateMotion(owner.getObjectId(), old);
			}
			motion.setActive(true);
			MotionDAO.updateMotion(owner.getObjectId(), motion);
		} else if (activeMotions != null) {
			Motion old = activeMotions.remove(motionType);
			if (old == null)
				return; // TODO packet hack??
			old.setActive(false);
			MotionDAO.updateMotion(owner.getObjectId(), old);
		}
		PacketSendUtility.sendPacket(owner, new SM_MOTION((short) motionId, (byte) motionType));
		PacketSendUtility.broadcastPacket(owner, new SM_MOTION(owner.getObjectId(), activeMotions), true);
	}
}
