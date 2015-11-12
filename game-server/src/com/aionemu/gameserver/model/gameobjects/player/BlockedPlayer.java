package com.aionemu.gameserver.model.gameobjects.player;

/**
 * Represents a player who has been blocked
 * 
 * @author Ben
 */
public class BlockedPlayer {

	PlayerCommonData pcd;
	String reason;

	public BlockedPlayer(PlayerCommonData pcd) {
		this(pcd, "");
	}

	public BlockedPlayer(PlayerCommonData pcd, String reason) {
		this.pcd = pcd;
		this.reason = reason;
	}

	public int getObjId() {
		return pcd.getPlayerObjId();
	}

	public String getName() {
		return pcd.getName();
	}

	public synchronized String getReason() {
		return reason;
	}

	public synchronized void setReason(String reason) {
		this.reason = reason;
	}
}
