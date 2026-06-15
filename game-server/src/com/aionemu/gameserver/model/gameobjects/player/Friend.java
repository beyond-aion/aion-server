package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.world.World;

/**
 * @author Ben
 */
public class Friend {

	private PlayerCommonData pcd;
	private String memo;

	public Friend(PlayerCommonData pcd, String memo) {
		this.pcd = pcd;
		this.memo = memo;
	}

	public Status getStatus() {
		if (!pcd.isOnline())
			return FriendList.Status.OFFLINE;
		Player player = World.getInstance().getPlayer(getObjectId());
		if (player == null)
			return FriendList.Status.OFFLINE;
		return player.getFriendList().getStatus();
	}

	public void setPCD(PlayerCommonData pcd) {
		this.pcd = pcd;
	}

	public String getName() {
		return pcd.getName();
	}

	public int getLevel() {
		return pcd.getLevel();
	}

	public String getNote() {
		return pcd.getNote();
	}

	public PlayerClass getPlayerClass() {
		return pcd.getPlayerClass();
	}

	public Gender getGender() {
		return pcd.getGender();
	}

	public int getMapId() {
		return pcd.getMapId();
	}

	public int getLastOnlineEpochSeconds() {
		return pcd.getLastOnlineEpochSeconds();
	}

	public int getObjectId() {
		return pcd.getPlayerObjId();
	}

	public synchronized String getFriendMemo() {
		return memo;
	}

	public synchronized void setFriendMemo(String memo) {
		this.memo = memo;
	}
}
