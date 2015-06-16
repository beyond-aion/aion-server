package com.aionemu.gameserver.model.instance.instancereward;

/**
 * @author xTz
 */
@SuppressWarnings("rawtypes")
public class DarkPoetaReward extends InstanceReward {

	private int points;
	private int npcKills;
	private int rank = 7;
	private int collections; // to do

	public DarkPoetaReward(Integer mapId, int instanceId) {
		super(mapId, instanceId);
	}

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
