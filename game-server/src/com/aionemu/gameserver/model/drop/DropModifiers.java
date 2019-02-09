package com.aionemu.gameserver.model.drop;

import com.aionemu.gameserver.model.Race;

public class DropModifiers {

	private boolean isDropNpcChest;
	private Race dropRace;
	private float boostDropRate;
	private Float reductionDropRate;
	private Integer maxDropsPerGroup;

	public boolean isDropNpcChest() {
		return isDropNpcChest;
	}

	public void setIsDropNpcChest(boolean dropNpcChest) {
		isDropNpcChest = dropNpcChest;
	}

	public Race getDropRace() {
		return dropRace;
	}

	public void setDropRace(Race dropRace) {
		this.dropRace = dropRace;
	}

	public float getBoostDropRate() {
		return boostDropRate;
	}

	public void setBoostDropRate(float boostDropRate) {
		this.boostDropRate = boostDropRate;
	}

	public Float getReductionDropRate() {
		return reductionDropRate;
	}

	public void setReductionDropRate(Float reductionDropRate) {
		this.reductionDropRate = reductionDropRate;
	}

	public Integer getMaxDropsPerGroup() {
		return maxDropsPerGroup;
	}

	public void setMaxDropsPerGroup(Integer maxDropsPerGroup) {
		this.maxDropsPerGroup = maxDropsPerGroup;
	}

	public float calculateDropChance(float chance, boolean allowReductionDropRate) {
		if (allowReductionDropRate && reductionDropRate != null)
			chance *= reductionDropRate;
		return chance * boostDropRate;
	}
}
