package com.aionemu.gameserver.model.instance.instancescore;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceScore<T extends InstancePlayerReward> {

	private final List<T> playerRewards = new ArrayList<>();
	private InstanceProgressionType instanceProgressionType = InstanceProgressionType.START_PROGRESS;

	public List<T> getPlayerRewards() {
		return playerRewards;
	}

	public boolean containsPlayer(int objectId) {
		return getPlayerReward(objectId) != null;
	}

	public void removePlayerReward(T reward) {
		playerRewards.remove(reward);
	}

	public T getPlayerReward(int objectId) {
		for (T playerReward : playerRewards) {
			if (playerReward.getOwnerId() == objectId) {
				return playerReward;
			}
		}
		return null;
	}

	public void addPlayerReward(T reward) {
		playerRewards.add(reward);
	}

	public void setInstanceProgressionType(InstanceProgressionType instanceProgressionType) {
		this.instanceProgressionType = instanceProgressionType;
	}

	public InstanceProgressionType getInstanceProgressionType() {
		return instanceProgressionType;
	}

	public boolean isRewarded() {
		return instanceProgressionType.isEndProgress();
	}

	public boolean isReinforcing() {
		return instanceProgressionType.isReinforcing();
	}

	public boolean isPreparing() {
		return instanceProgressionType.isPreparing();
	}

	public boolean isStartProgress() {
		return instanceProgressionType.isStartProgress();
	}

	public void clear() {
		playerRewards.clear();
	}

}
