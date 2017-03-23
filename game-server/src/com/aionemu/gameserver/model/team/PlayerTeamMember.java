package com.aionemu.gameserver.model.team;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerTeamMember implements TeamMember<Player> {

	final Player player;

	private long lastOnlineTime;

	public PlayerTeamMember(Player player) {
		this.player = player;
	}

	@Override
	public int getObjectId() {
		return player.getObjectId();
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public Player getObject() {
		return player;
	}

	public long getLastOnlineTime() {
		return lastOnlineTime;
	}

	public void updateLastOnlineTime() {
		lastOnlineTime = System.currentTimeMillis();
	}

	public boolean isOnline() {
		return player.isOnline();
	}

	public float getX() {
		return player.getX();
	}

	public float getY() {
		return player.getY();
	}

	public float getZ() {
		return player.getZ();
	}

	public byte getHeading() {
		return player.getHeading();
	}

	public byte getLevel() {
		return player.getLevel();
	}
}
