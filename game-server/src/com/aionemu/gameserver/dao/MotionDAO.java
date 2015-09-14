package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;

/**
 * @author MrPoke
 */
public abstract class MotionDAO implements DAO {

	public abstract void loadMotionList(Player player);

	public abstract boolean storeMotion(int objectId, Motion motion);

	public abstract boolean updateMotion(int objectId, Motion motion);

	public abstract boolean deleteMotion(int objectId, int motionId);

	@Override
	public String getClassName() {
		return MotionDAO.class.getName();
	}
}
