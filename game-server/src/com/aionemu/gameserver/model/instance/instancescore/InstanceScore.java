package com.aionemu.gameserver.model.instance.instancescore;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceScore<T extends InstancePlayerReward> {

	private final Map<Integer, T> playerRewards = new ConcurrentHashMap<>();
	private InstanceProgressionType instanceProgressionType = InstanceProgressionType.START_PROGRESS;

	public Collection<T> getPlayerRewards() {
		return playerRewards.values();
	}

	public boolean containsPlayer(int objectId) {
		return playerRewards.containsKey(objectId);
	}

	public void removePlayerReward(T reward) {
		playerRewards.remove(reward.getOwnerId());
	}

	public T getPlayerReward(int objectId) {
		return playerRewards.get(objectId);
	}

	public void addPlayerReward(T reward) {
		playerRewards.put(reward.getOwnerId(), reward);
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
