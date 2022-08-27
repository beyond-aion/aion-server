package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class DarkPoetaReward extends InstanceReward<InstancePlayerReward> {

	private int points;
	private int npcKills;
	private int rank = 7;
	private int collections;

	public void addPoints(int points) {
		this.points += points;
	}

	public int getPoints() {
		return points;
	}

	public void addNpcKill() {
		npcKills++;
	}

	public int getNpcKills() {
		return npcKills;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public void addGather() {
		collections++;
	}

	public int getGatherCollections() {
		return collections;
	}
}
