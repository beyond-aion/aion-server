package com.aionemu.gameserver.model.instance;

/**
 * @author xTz
 */
public enum InstanceProgressionType {
	REINFORCE_MEMBER(12 * 1024 * 1024), // 12582912
	PREPARING(1 * 1024 * 1024), // 1048576
	START_PROGRESS(2 * 1024 * 1024), // 2097152
	END_PROGRESS(3 * 1024 * 1024); // 3145728

	private int id;

	private InstanceProgressionType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isReinforcing() {
		return id == REINFORCE_MEMBER.id;
	}

	public boolean isPreparing() {
		return id == PREPARING.id;
	}

	public boolean isStartProgress() {
		return id == START_PROGRESS.id;
	}

	public boolean isEndProgress() {
		return id == END_PROGRESS.id;
	}
}
