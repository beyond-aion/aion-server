package com.aionemu.gameserver.model.instance.instancereward;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceReward<T extends InstancePlayerReward> {

	private final List<T> instanceRewards = new ArrayList<>();
	private InstanceProgressionType instanceProgressionType = InstanceProgressionType.START_PROGRESS;

	public List<T> getInstanceRewards() {
		return instanceRewards;
	}

	public boolean containsPlayer(int objectId) {
		return getPlayerReward(objectId) != null;
	}

	public void removePlayerReward(T reward) {
		instanceRewards.remove(reward);
	}

	public T getPlayerReward(int objectId) {
		for (T instanceReward : instanceRewards) {
			if (instanceReward.getOwnerId() == objectId) {
				return instanceReward;
			}
		}
		return null;
	}

	public void addPlayerReward(T reward) {
		instanceRewards.add(reward);
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
		instanceRewards.clear();
	}

}
