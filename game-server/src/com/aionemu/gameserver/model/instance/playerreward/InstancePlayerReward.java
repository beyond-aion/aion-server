package com.aionemu.gameserver.model.instance.playerreward;

/**
 *
 * @author xTz
 */
public class InstancePlayerReward {

	private int points;
	private int playerPvPKills;
	private int playerMonsterKills;
	protected Integer object;

	public InstancePlayerReward(Integer object) {
		this.object = object;
	}

	public Integer getOwner() {
		return object;
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
		if (this.points < 0) {
			this.points = 0;
		}
	}

	public void addPvPKillToPlayer() {
		playerPvPKills ++;
	}

	public void addMonsterKillToPlayer() {
		playerMonsterKills ++;
	}
}
