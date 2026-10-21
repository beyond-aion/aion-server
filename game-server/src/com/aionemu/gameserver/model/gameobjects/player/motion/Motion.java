package com.aionemu.gameserver.model.gameobjects.player.motion;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public class Motion implements Expirable {

	private final int id;
	private final int deletionTime;
	private boolean active;

	public Motion(int id, int deletionTime, boolean isActive) {
		this.id = id;
		this.deletionTime = deletionTime;
		this.active = isActive;
	}

	public int getId() {
		return id;
	}

	public MotionType getType() {
		return DataManager.ITEM_DATA.getMotionType(id);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int getExpireTime() {
		return deletionTime;
	}

	@Override
	public void onExpire(Player player) {
		player.getMotions().remove(id);
	}
}
