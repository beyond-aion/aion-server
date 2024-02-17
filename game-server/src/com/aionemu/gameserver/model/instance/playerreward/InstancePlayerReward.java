package com.aionemu.gameserver.model.instance.playerreward;

/**
 * @author xTz
 */
public class InstancePlayerReward {

	private int points;
	private int playerPvPKills;
	private int playerMonsterKills;
	private final int objectId;

	public InstancePlayerReward(int objectId) {
		this.objectId = objectId;
	}

	public int getOwnerId() {
		return objectId;
	}

	public int getPoints() {
		return points;
	}

	public int getPvPKills() {
		return playerPvPKills;
	}

	public int getMonsterKills() {
		return playerMonsterKills;
	}

	public void addPoints(int points) {
		this.points += points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void addPvPKill() {
		playerPvPKills++;
	}

	public void addMonsterKillToPlayer() {
		playerMonsterKills++;
	}
}
