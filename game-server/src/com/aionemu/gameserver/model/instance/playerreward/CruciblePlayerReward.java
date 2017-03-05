package com.aionemu.gameserver.model.instance.playerreward;

/**
 * @author xTz
 */
public class CruciblePlayerReward extends InstancePlayerReward {

	private int spawnPosition;
	private boolean isRewarded = false;
	private int insignia;
	private boolean isPlayerLeave = false;
	private boolean isPlayerDefeated = false;

	public CruciblePlayerReward(int objectId) {
		super(objectId);
	}

	public boolean isRewarded() {
		return isRewarded;
	}

	public void setRewarded() {
		isRewarded = true;
	}

	public void setInsignia(int insignia) {
		this.insignia = insignia;
	}

	public int getInsignia() {
		return insignia;
	}

	public void setSpawnPosition(int spawnPosition) {
		this.spawnPosition = spawnPosition;
	}

	public int getSpawnPosition() {
		return spawnPosition;
	}

	public boolean isPlayerLeave() {
		return isPlayerLeave;
	}

	public void setPlayerLeave() {
		isPlayerLeave = true;
	}

	public void setPlayerDefeated(boolean value) {
		isPlayerDefeated = value;
	}

	public boolean isPlayerDefeated() {
		return isPlayerDefeated;
	}
}
