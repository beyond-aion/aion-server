package com.aionemu.gameserver.model.instance.instancereward;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceReward<T extends InstancePlayerReward> {

	protected final int mapId;
	protected final int instanceId;
	private final List<T> instanceRewards = new ArrayList<>();
	private InstanceScoreType instanceScoreType = InstanceScoreType.START_PROGRESS;

	public InstanceReward(int mapId, int instanceId) {
		this.mapId = mapId;
		this.instanceId = instanceId;
	}

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

	public void setInstanceScoreType(InstanceScoreType instanceScoreType) {
		this.instanceScoreType = instanceScoreType;
	}

	public InstanceScoreType getInstanceScoreType() {
		return instanceScoreType;
	}

	public int getMapId() {
		return mapId;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public boolean isRewarded() {
		return instanceScoreType.isEndProgress();
	}

	public boolean isReinforcing() {
		return instanceScoreType.isReinforsing();
	}

	public boolean isPreparing() {
		return instanceScoreType.isPreparing();
	}

	public boolean isStartProgress() {
		return instanceScoreType.isStartProgress();
	}

	public void clear() {
		instanceRewards.clear();
	}

}
