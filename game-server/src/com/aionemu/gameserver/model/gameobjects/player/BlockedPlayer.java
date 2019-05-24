package com.aionemu.gameserver.model.gameobjects.player;

/**
 * Represents a player who has been blocked
 * 
 * @author Ben
 */
public class BlockedPlayer {

	private final int objId;
	private final String name;
	private String reason;

	public BlockedPlayer(int objId, String name, String reason) {
		this.objId = objId;
		this.name = name;
		this.reason = reason;
	}

	public int getObjId() {
		return objId;
	}

	public String getName() {
		return name;
	}

	public synchronized String getReason() {
		return reason;
	}

	public synchronized void setReason(String reason) {
		this.reason = reason;
	}
}
