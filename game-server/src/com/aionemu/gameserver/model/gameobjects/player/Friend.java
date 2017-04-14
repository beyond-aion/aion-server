package com.aionemu.gameserver.model.gameobjects.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ben
 */
public class Friend {

	private static final Logger log = LoggerFactory.getLogger(Friend.class);
	private PlayerCommonData pcd;
	private String memo;

	public Friend(PlayerCommonData pcd, String memo) {
		this.pcd = pcd;
		this.memo = memo;
	}

	/**
	 * Returns the status of this player
	 * 
	 * @return Friend's status
	 */
	public Status getStatus() {
		if (!pcd.isOnline())
			return FriendList.Status.OFFLINE;
		Player player = World.getInstance().findPlayer(getObjectId());
		if (player == null)
			return FriendList.Status.OFFLINE;
		return player.getFriendList().getStatus();
	}

	public void setPCD(PlayerCommonData pcd) {
		this.pcd = pcd;
	}

	/**
	 * Returns this friend's name
	 * 
	 * @return Friend's name
	 */
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
		WorldPosition position = pcd.getPosition();
		if (position == null) {
			// doubt its possible, but need check warnings
			log.warn("Null friend position: {}", pcd.getPlayerObjId());
			return 0;
		}
		return position.getMapId();
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
