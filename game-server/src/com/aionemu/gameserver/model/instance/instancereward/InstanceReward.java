package com.aionemu.gameserver.model.instance.instancereward;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author xTz
 */
public class InstanceReward<T extends InstancePlayerReward> {

	protected FastTable<T> instanceRewards = new FastTable<>();
	private InstanceScoreType instanceScoreType = InstanceScoreType.START_PROGRESS;
	protected Integer mapId;
	protected int instanceId;

	public InstanceReward(Integer mapId, int instanceId) {
		this.mapId = mapId;
		this.instanceId = instanceId;
	}

	public FastTable<T> getInstanceRewards() {
		return instanceRewards;
	}

	public boolean containPlayer(Integer object) {
		for (InstancePlayerReward instanceReward : instanceRewards) {
			if (instanceReward.getOwner().equals(object)) {
				return true;
			}
		}
		return false;
	}

	public void removePlayerReward(T reward) {
		if (instanceRewards.contains(reward)) {
			instanceRewards.remove(reward);
		}
	}

	public InstancePlayerReward getPlayerReward(Integer object) {
		for (InstancePlayerReward instanceReward : instanceRewards) {
			if (instanceReward.getOwner().equals(object)) {
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

	public Integer getMapId() {
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

	protected InstanceReward<?> getInstanceReward() {
		return this;
	}
}
